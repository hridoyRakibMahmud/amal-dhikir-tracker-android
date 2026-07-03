package com.example.amaldhikirtracker.data.repository

import com.example.amaldhikirtracker.data.local.dao.AmalDao
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AmalRepository(private val amalDao: AmalDao) {

    fun getSalatLogsByDate(date: LocalDate): Flow<List<SalatLog>> =
        amalDao.getSalatLogsByDate(date)

    suspend fun insertSalatLog(salatLog: SalatLog) =
        amalDao.insertSalatLog(salatLog)

    suspend fun getSalatLogByName(name: String, date: LocalDate): SalatLog? =
        amalDao.getSalatLogByName(name, date)

    fun getSalatLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SalatLog>> =
        amalDao.getSalatLogsInRange(startDate, endDate)

    fun getAllDhikirs(): Flow<List<Dhikir>> =
        amalDao.getAllDhikirs()

    suspend fun getDhikirById(id: Long): Dhikir? =
        amalDao.getDhikirById(id)

    suspend fun insertDhikir(dhikir: Dhikir) =
        amalDao.insertDhikir(dhikir)

    suspend fun updateDhikir(dhikir: Dhikir) =
        amalDao.updateDhikir(dhikir)

    suspend fun deleteDhikir(dhikir: Dhikir) =
        amalDao.deleteDhikir(dhikir)

    fun getDhikirLogsByDate(date: LocalDate): Flow<List<DhikirLog>> =
        amalDao.getDhikirLogsByDate(date)

    suspend fun insertDhikirLog(dhikirLog: DhikirLog) =
        amalDao.insertDhikirLog(dhikirLog)

    suspend fun upsertDhikirLog(dhikirLog: DhikirLog) =
        amalDao.upsertDhikirLog(dhikirLog)

    fun getDhikirLogsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DhikirLog>> =
        amalDao.getDhikirLogsInRange(startDate, endDate)
}
