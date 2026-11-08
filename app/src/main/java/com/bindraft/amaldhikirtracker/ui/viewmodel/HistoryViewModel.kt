package com.bindraft.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.util.LocationTracker
import com.bindraft.amaldhikirtracker.util.SpiritualDayProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields

private const val DAYS_IN_WEEK = 7L

class HistoryViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker,
    private val spiritualDay: SpiritualDayProvider
) : ViewModel() {

    // Once the user pages the chart, stop following the day's rollover (same convention as Quran/Fasting).
    private var userNavigatedWeek = false

    private val _weekStart = MutableStateFlow(startOfWeek(spiritualDay.date.value))

    init {
        viewModelScope.launch {
            spiritualDay.date.collect { today ->
                if (!userNavigatedWeek) _weekStart.value = startOfWeek(today)
            }
        }
    }

    fun nextWeek() {
        userNavigatedWeek = true
        _weekStart.value = _weekStart.value.plusWeeks(1)
    }

    fun previousWeek() {
        userNavigatedWeek = true
        _weekStart.value = _weekStart.value.minusWeeks(1)
    }

    /** ISO week-of-year and week-based year (Monday-first) for the visible week. */
    val weekNumber: StateFlow<Int> = _weekStart.map { it.get(WeekFields.ISO.weekOfWeekBasedYear()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val weekYear: StateFlow<Int> = _weekStart.map { it.get(WeekFields.ISO.weekBasedYear()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private fun startOfWeek(date: LocalDate): LocalDate =
        date.with(WeekFields.ISO.dayOfWeek(), 1)

    val allDhikirs: StateFlow<List<Dhikir>> = repository.getAllDhikirs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDhikirId = MutableStateFlow<Long?>(null)
    val selectedDhikirId: StateFlow<Long?> = _selectedDhikirId.asStateFlow()

    fun selectDhikir(dhikirId: Long?) {
        _selectedDhikirId.value = dhikirId
    }

    val historyState: StateFlow<HistoryState> = _weekStart.flatMapLatest { startDate ->
        val endDate = startDate.plusDays(DAYS_IN_WEEK - 1)

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
        private val locationTracker: LocationTracker,
        private val spiritualDay: SpiritualDayProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: java.lang.Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository, preferencesManager, locationTracker, spiritualDay) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
