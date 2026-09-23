package com.example.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import com.example.amaldhikirtracker.data.repository.AmalRepository
import com.example.amaldhikirtracker.data.local.PreferencesManager
import com.example.amaldhikirtracker.util.LocationTracker
import com.example.amaldhikirtracker.util.resolveSpiritualDate
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class HistoryViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _dateRange = MutableStateFlow(7)
    val dateRange: StateFlow<Int> = _dateRange.asStateFlow()

    val historyState: StateFlow<HistoryState> = _dateRange.flatMapLatest { days ->
        val endDate = resolveSpiritualDate(preferencesManager, locationTracker)
        val startDate = endDate.minusDays(days.toLong() - 1)

        combine(
            repository.getSalatLogsInRange(startDate, endDate),
            repository.getDhikirLogsInRange(startDate, endDate),
            repository.getAllDhikirs()
        ) { salatLogs, dhikirLogs, allDhikirs ->
            val salatGrouped = salatLogs.groupBy { it.date }
            val dhikirGrouped = dhikirLogs.groupBy { it.date }
            
            // Calculate Stats
            val totalSalats = salatLogs.size
            val completedSalats = salatLogs.count { it.isCompleted }
            val completionRate = if (totalSalats > 0) completedSalats.toFloat() / totalSalats else 0f
            
            val totalDhikirCount = dhikirLogs.sumOf { it.count }
            
            HistoryState(
                salatLogs = salatGrouped,
                dhikirLogs = dhikirGrouped,
                dhikirMap = allDhikirs.associateBy { it.id },
                startDate = startDate,
                endDate = endDate,
                completionRate = completionRate,
                totalDhikirCount = totalDhikirCount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryState())

    fun setDateRange(days: Int) {
        _dateRange.value = days
    }

    data class HistoryState(
        val salatLogs: Map<LocalDate, List<SalatLog>> = emptyMap(),
        val dhikirLogs: Map<LocalDate, List<DhikirLog>> = emptyMap(),
        val dhikirMap: Map<Long, Dhikir> = emptyMap(),
        val startDate: LocalDate = LocalDate.now(),
        val endDate: LocalDate = LocalDate.now(),
        val completionRate: Float = 0f,
        val totalDhikirCount: Int = 0
    )

    class Factory(
        private val repository: AmalRepository,
        private val preferencesManager: PreferencesManager,
        private val locationTracker: LocationTracker
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: java.lang.Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository, preferencesManager, locationTracker) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
