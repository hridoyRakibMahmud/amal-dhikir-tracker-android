package com.bindraft.amaldhikirtracker.data.repository

import com.bindraft.amaldhikirtracker.data.local.dao.AmalDao
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.local.entities.QuranLog
import com.bindraft.amaldhikirtracker.data.local.entities.QuranMode
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Single choke point for all writes: each write goes to Room first, then to [SyncManager].
 * Reads come straight from Room, so ViewModels are unaffected by sync.
 *
 * Salat, dhikir-log and fasting-log rows use deterministic syncIds (name/date based), so the
 * same record created on two devices merges instead of duplicating.
 */
class AmalRepository(
    private val amalDao: AmalDao,
    private val syncManager: SyncManager
) {

    private fun now() = System.currentTimeMillis()

    private suspend fun dhikirLogSyncId(dhikirId: Long, date: LocalDate): String =
        "${amalDao.getDhikirById(dhikirId)?.syncId ?: dhikirId}_$date"

    fun getSalatLogsByDate(date: LocalDate): Flow<List<SalatLog>> =
        amalDao.getSalatLogsByDate(date)

    suspend fun insertSalatLog(salatLog: SalatLog) {
        val stamped = salatLog.copy(syncId = "${salatLog.salatName}_${salatLog.date}", updatedAt = now())
        amalDao.insertSalatLog(stamped)
        syncManager.pushSalatLog(stamped)
    }

    suspend fun getSalatLogByName(name: String, date: LocalDate): SalatLog? =
        amalDao.getSalatLogByName(name, date)

    fun getSalatLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SalatLog>> =
        amalDao.getSalatLogsInRange(startDate, endDate)

    fun getAllDhikirs(): Flow<List<Dhikir>> =
        amalDao.getAllDhikirs()

    suspend fun getDhikirById(id: Long): Dhikir? =
        amalDao.getDhikirById(id)

    suspend fun insertDhikir(dhikir: Dhikir): Long {
        val stamped = dhikir.copy(updatedAt = now())
        val id = amalDao.insertDhikir(stamped)
        syncManager.pushDhikir(stamped.copy(id = id))
        return id
    }

    suspend fun updateDhikir(dhikir: Dhikir) {
        val stamped = dhikir.copy(updatedAt = now())
        amalDao.updateDhikir(stamped)
        syncManager.pushDhikir(stamped)
    }

    suspend fun deleteDhikir(dhikir: Dhikir) {
        amalDao.deleteDhikir(dhikir)
        syncManager.deleteRemote("dhikirs", dhikir.syncId)
    }

    /** Renames a dhikir and gives it a new syncId, relinking all its logs to match (same pattern as [changeFastingTypeSyncId]). */
    suspend fun changeDhikirSyncId(dhikir: Dhikir, newSyncId: String, newName: String? = null) {
        val oldSyncId = dhikir.syncId
        val updated = dhikir.copy(syncId = newSyncId, name = newName ?: dhikir.name, updatedAt = now())
        amalDao.updateDhikir(updated)
        syncManager.deleteRemote("dhikirs", oldSyncId)
        syncManager.pushDhikir(updated)
        amalDao.getDhikirLogsForDhikir(updated.id).forEach { log ->
            val relinked = log.copy(syncId = "${updated.syncId}_${log.date}", updatedAt = now())
            amalDao.insertDhikirLog(relinked)
            syncManager.pushDhikirLog(relinked)
        }
    }

    /** Adds [duplicateId]'s logged counts onto [primaryId] (same date = summed), leaving duplicate's own logs untouched for the caller to clean up. */
    suspend fun mergeDhikirLogsInto(primaryId: Long, duplicateId: Long) {
        amalDao.getDhikirLogsForDhikir(duplicateId).forEach { log ->
            upsertDhikirLog(DhikirLog(dhikirId = primaryId, count = log.count, date = log.date))
        }
    }

    fun getTotalCountForDhikir(dhikirId: Long): Flow<Int> =
        amalDao.getTotalCountForDhikir(dhikirId)

    suspend fun setDhikirTarget(dhikirId: Long, dailyTarget: Int?) {
        amalDao.setDhikirTarget(dhikirId, dailyTarget, now())
        amalDao.getDhikirById(dhikirId)?.let { syncManager.pushDhikir(it) }
    }

    fun getDhikirLogsByDate(date: LocalDate): Flow<List<DhikirLog>> =
        amalDao.getDhikirLogsByDate(date)

    suspend fun insertDhikirLog(dhikirLog: DhikirLog) {
        val stamped = dhikirLog.copy(syncId = dhikirLogSyncId(dhikirLog.dhikirId, dhikirLog.date), updatedAt = now())
        amalDao.insertDhikirLog(stamped)
        syncManager.pushDhikirLog(stamped)
    }

    suspend fun upsertDhikirLog(dhikirLog: DhikirLog) {
        amalDao.upsertDhikirLog(
            dhikirLog.copy(syncId = dhikirLogSyncId(dhikirLog.dhikirId, dhikirLog.date))
        )
        amalDao.getDhikirLog(dhikirLog.dhikirId, dhikirLog.date)?.let { syncManager.pushDhikirLog(it) }
    }

    fun getDhikirLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DhikirLog>> =
        amalDao.getDhikirLogsInRange(startDate, endDate)

    fun getAllFastingTypes(): Flow<List<FastingType>> =
        amalDao.getAllFastingTypes()

    suspend fun insertFastingType(fastingType: FastingType): Long {
        val stamped = fastingType.copy(updatedAt = now())
        val id = amalDao.insertFastingType(stamped)
        syncManager.pushFastingType(stamped.copy(id = id))
        return id
    }

    suspend fun changeFastingTypeSyncId(fastingType: FastingType, newSyncId: String) {
        val oldSyncId = fastingType.syncId
        val updated = fastingType.copy(syncId = newSyncId, updatedAt = now())
        amalDao.insertFastingType(updated)
        syncManager.deleteRemote("fastingTypes", oldSyncId)
        syncManager.pushFastingType(updated)
        amalDao.getFastingLogsForType(updated.id).forEach { syncManager.pushFastingLog(it) }
    }

    suspend fun deleteFastingType(fastingType: FastingType) {
        amalDao.deleteFastingType(fastingType)
        syncManager.deleteRemote("fastingTypes", fastingType.syncId)
    }

    suspend fun getFastingLog(date: LocalDate): FastingLog? =
        amalDao.getFastingLog(date)

    suspend fun insertFastingLog(fastingLog: FastingLog) {
        val stamped = fastingLog.copy(syncId = "fast_${fastingLog.date}", updatedAt = now())
        amalDao.insertFastingLog(stamped)
        syncManager.pushFastingLog(stamped)
    }

    suspend fun deleteFastingLog(fastingLog: FastingLog) {
        amalDao.deleteFastingLog(fastingLog)
        syncManager.deleteRemote("fastingLogs", fastingLog.syncId)
    }

    fun getFastingLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<FastingLog>> =
        amalDao.getFastingLogsInRange(startDate, endDate)

    private suspend fun quranLogSyncId(quranModeId: Long, date: LocalDate): String =
        "${amalDao.getQuranModeById(quranModeId)?.syncId ?: quranModeId}_$date"

    fun getAllQuranModes(): Flow<List<QuranMode>> =
        amalDao.getAllQuranModes()

    suspend fun insertQuranMode(quranMode: QuranMode): Long {
        val stamped = quranMode.copy(updatedAt = now())
        val id = amalDao.insertQuranMode(stamped)
        syncManager.pushQuranMode(stamped.copy(id = id))
        return id
    }

    suspend fun setQuranModeTarget(quranModeId: Long, dailyTarget: Int?) {
        amalDao.setQuranModeTarget(quranModeId, dailyTarget, now())
        amalDao.getQuranModeById(quranModeId)?.let { syncManager.pushQuranMode(it) }
    }

    fun getQuranLogsByDate(date: LocalDate): Flow<List<QuranLog>> =
        amalDao.getQuranLogsByDate(date)

    suspend fun upsertQuranLog(quranLog: QuranLog) {
        amalDao.upsertQuranLog(
            quranLog.copy(syncId = quranLogSyncId(quranLog.quranModeId, quranLog.date))
        )
        amalDao.getQuranLog(quranLog.quranModeId, quranLog.date)?.let { syncManager.pushQuranLog(it) }
    }

    fun getQuranLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<QuranLog>> =
        amalDao.getQuranLogsInRange(startDate, endDate)

    /** Overwrites (not adds to) a day's total — used when editing an already-logged day. Zero clears it. */
    suspend fun setQuranLog(quranModeId: Long, date: LocalDate, ayahsRead: Int) {
        val existing = amalDao.getQuranLog(quranModeId, date)
        if (ayahsRead <= 0) {
            existing?.let {
                amalDao.deleteQuranLog(it)
                syncManager.deleteRemote("quranLogs", it.syncId)
            }
            return
        }
        val stamped = (existing ?: QuranLog(quranModeId = quranModeId, ayahsRead = 0, date = date))
            .copy(ayahsRead = ayahsRead, syncId = quranLogSyncId(quranModeId, date), updatedAt = now())
        amalDao.insertQuranLog(stamped)
        syncManager.pushQuranLog(stamped)
    }
}
