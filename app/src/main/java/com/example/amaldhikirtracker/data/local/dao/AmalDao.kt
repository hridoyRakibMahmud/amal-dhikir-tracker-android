package com.example.amaldhikirtracker.data.local.dao

import androidx.room.*
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.FastingLog
import com.example.amaldhikirtracker.data.local.entities.FastingType
import com.example.amaldhikirtracker.data.local.entities.SalatLog
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

    @Query("UPDATE dhikirs SET dailyTarget = :dailyTarget WHERE id = :dhikirId")
    suspend fun setDhikirTarget(dhikirId: Long, dailyTarget: Int?)

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
        val existing = getDhikirLog(dhikirLog.dhikirId, dhikirLog.date)
        if (existing == null) {
            insertDhikirLog(dhikirLog)
        } else {
            insertDhikirLog(existing.copy(count = existing.count + dhikirLog.count))
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
}
