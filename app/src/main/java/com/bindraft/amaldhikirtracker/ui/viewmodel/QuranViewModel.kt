package com.bindraft.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.QuranLog
import com.bindraft.amaldhikirtracker.data.local.entities.QuranMode
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.util.LocationTracker
import com.bindraft.amaldhikirtracker.util.SpiritualDayProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields

private const val DAYS_IN_WEEK = 7L

class QuranViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker,
    private val spiritualDay: SpiritualDayProvider
) : ViewModel() {

    val spiritualDate: StateFlow<LocalDate> = spiritualDay.date

    val modes: StateFlow<List<QuranMode>> = repository.getAllQuranModes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedModeId = MutableStateFlow<Long?>(null)

    fun selectMode(quranModeId: Long) {
        _selectedModeId.value = quranModeId
    }

    /** The user's choice if it still exists, else the first seeded mode. */
    val selectedMode: StateFlow<QuranMode?> = combine(modes, _selectedModeId) { modes, selectedId ->
        modes.find { it.id == selectedId } ?: modes.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** quranModeId -> ayahs read on the current spiritual day. */
    val todayLogs: StateFlow<Map<Long, Int>> = spiritualDate
        .flatMapLatest { date -> repository.getQuranLogsByDate(date) }
        .map { logs -> logs.associate { it.quranModeId to it.ayahsRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val todayAyahsForSelected: StateFlow<Int> = combine(todayLogs, selectedMode) { logs, mode ->
        mode?.let { logs[it.id] } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Once the user pages the chart, stop following the day's rollover (same convention as Fasting's calendar).
    private var userNavigatedWeek = false

    private val _weekStart = MutableStateFlow(startOfWeek(spiritualDay.date.value))

    init {
        viewModelScope.launch {
            spiritualDate.collect { today ->
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

    /** e.g. "Week 23 · 2026". ISO week numbering (Monday-first). */
    val weekLabel: StateFlow<String> = _weekStart.map { start ->
        val weekNumber = start.get(WeekFields.ISO.weekOfWeekBasedYear())
        val weekYear = start.get(WeekFields.ISO.weekBasedYear())
        "Week $weekNumber · $weekYear"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** The 7 days of the visible week (Monday first), oldest first, with ayahs read for the selected mode, zero-filled. */
    val weekDailyAyahs: StateFlow<List<Pair<LocalDate, Int>>> = combine(selectedMode, _weekStart) { mode, start -> mode to start }
        .flatMapLatest { (mode, start) ->
            if (mode == null) {
                flowOf(emptyList())
            } else {
                val end = start.plusDays(DAYS_IN_WEEK - 1)
                repository.getQuranLogsInRange(start, end).map { logs ->
                    val byDate = logs.filter { it.quranModeId == mode.id }.associate { it.date to it.ayahsRead }
                    (0 until DAYS_IN_WEEK).map { offset ->
                        val date = start.plusDays(offset)
                        date to (byDate[date] ?: 0)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun startOfWeek(date: LocalDate): LocalDate =
        date.with(WeekFields.ISO.dayOfWeek(), 1)

    /** Adds to whatever is already logged. Defaults to today; pass an earlier date to log retroactively. */
    fun addAyahs(quranModeId: Long, amount: Int, date: LocalDate = spiritualDate.value) {
        if (amount <= 0) return
        if (date.isAfter(spiritualDate.value)) return
        viewModelScope.launch {
            repository.upsertQuranLog(QuranLog(quranModeId = quranModeId, ayahsRead = amount, date = date))
        }
    }

    /** Overwrites a specific day's total (e.g. tapping a chart bar to correct it). Zero clears the log. */
    fun editAyahs(quranModeId: Long, date: LocalDate, ayahsRead: Int) {
        if (date.isAfter(spiritualDate.value)) return
        viewModelScope.launch {
            repository.setQuranLog(quranModeId, date, ayahsRead)
        }
    }

    fun setTarget(quranModeId: Long, target: Int?) {
        viewModelScope.launch {
            repository.setQuranModeTarget(quranModeId, target)
        }
    }

    class Factory(
        private val repository: AmalRepository,
        private val preferencesManager: PreferencesManager,
        private val locationTracker: LocationTracker,
        private val spiritualDay: SpiritualDayProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuranViewModel(repository, preferencesManager, locationTracker, spiritualDay) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
