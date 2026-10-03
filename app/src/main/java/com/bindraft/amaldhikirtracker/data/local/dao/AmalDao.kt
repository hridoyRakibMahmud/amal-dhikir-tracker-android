package com.bindraft.amaldhikirtracker.data.local.dao

import androidx.room.*
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AmalDao {

    // Salat Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalatLog(salatLog: SalatLog)

    @Query("SELECT * FROM salat_logs WHERE date = :date")
    fun getSalatLogsByDate(date: LocalDate): Flow<List<SalatLog>>

    @Query("SELECT * FROM salat_logs WHERE salatName = :name AND date = :date")
    suspend fun getSalatLogByName(name: String, date: LocalDate): SalatLog?

    @Query("SELECT * FROM salat_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getSalatLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SalatLog>>

    // Dhikir
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikir(dhikir: Dhikir): Long

    @Update
    suspend fun updateDhikir(dhikir: Dhikir)

    @Delete
    suspend fun deleteDhikir(dhikir: Dhikir)

    @Query("SELECT * FROM dhikirs")
    fun getAllDhikirs(): Flow<List<Dhikir>>

    @Query("SELECT * FROM dhikirs WHERE id = :id")
    suspend fun getDhikirById(id: Long): Dhikir?

    @Query("SELECT COALESCE(SUM(count), 0) FROM dhikir_logs WHERE dhikirId = :dhikirId")
    fun getTotalCountForDhikir(dhikirId: Long): Flow<Int>

    @Query("UPDATE dhikirs SET dailyTarget = :dailyTarget, updatedAt = :updatedAt WHERE id = :dhikirId")
    suspend fun setDhikirTarget(dhikirId: Long, dailyTarget: Int?, updatedAt: Long)

    // Dhikir Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikirLog(dhikirLog: DhikirLog)

    @Query("SELECT * FROM dhikir_logs WHERE date = :date")
    fun getDhikirLogsByDate(date: LocalDate): Flow<List<DhikirLog>>

    @Query("SELECT * FROM dhikir_logs WHERE dhikirId = :dhikirId AND date = :date")
    suspend fun getDhikirLog(dhikirId: Long, date: LocalDate): DhikirLog?

    @Query("SELECT * FROM dhikir_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getDhikirLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DhikirLog>>

    @Transaction
    suspend fun upsertDhikirLog(dhikirLog: DhikirLog) {
        val now = System.currentTimeMillis()
        val existing = getDhikirLog(dhikirLog.dhikirId, dhikirLog.date)
        if (existing == null) {
            insertDhikirLog(dhikirLog.copy(updatedAt = now))
        } else {
            insertDhikirLog(existing.copy(count = existing.count + dhikirLog.count, updatedAt = now))
        }
    }

    // Fasting Types
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFastingType(fastingType: FastingType): Long

    @Delete
    suspend fun deleteFastingType(fastingType: FastingType)

    @Query("SELECT * FROM fasting_types")
    fun getAllFastingTypes(): Flow<List<FastingType>>

    // Fasting Logs — at most one per date
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFastingLog(fastingLog: FastingLog)

    @Delete
    suspend fun deleteFastingLog(fastingLog: FastingLog)

    @Query("SELECT * FROM fasting_logs WHERE date = :date LIMIT 1")
    suspend fun getFastingLog(date: LocalDate): FastingLog?

    @Query("SELECT * FROM fasting_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getFastingLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<FastingLog>>

    // Sync lookups — resolve remote syncId references back to local rows
    @Query("SELECT * FROM dhikirs WHERE syncId = :syncId LIMIT 1")
    suspend fun getDhikirBySyncId(syncId: String): Dhikir?

    @Query("SELECT * FROM dhikir_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getDhikirLogBySyncId(syncId: String): DhikirLog?

    @Query("SELECT * FROM salat_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getSalatLogBySyncId(syncId: String): SalatLog?

    @Query("SELECT * FROM fasting_types WHERE syncId = :syncId LIMIT 1")
    suspend fun getFastingTypeBySyncId(syncId: String): FastingType?

    @Query("SELECT * FROM fasting_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getFastingLogBySyncId(syncId: String): FastingLog?

    @Query("SELECT * FROM dhikir_logs")
    suspend fun getAllDhikirLogsOnce(): List<DhikirLog>

    @Query("SELECT * FROM salat_logs")
    suspend fun getAllSalatLogsOnce(): List<SalatLog>

    @Query("SELECT * FROM fasting_logs")
    suspend fun getAllFastingLogsOnce(): List<FastingLog>

    @Delete
    suspend fun deleteDhikirLog(dhikirLog: DhikirLog)

    @Delete
    suspend fun deleteSalatLog(salatLog: SalatLog)

    @Query("SELECT * FROM fasting_types WHERE id = :id")
    suspend fun getFastingTypeById(id: Long): FastingType?
}
