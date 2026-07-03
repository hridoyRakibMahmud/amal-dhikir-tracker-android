package com.example.amaldhikirtracker.data.local.dao

import androidx.room.*
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
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
}
