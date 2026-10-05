package com.bindraft.amaldhikirtracker.data.repository

import com.bindraft.amaldhikirtracker.data.local.dao.AmalDao
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
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
}
