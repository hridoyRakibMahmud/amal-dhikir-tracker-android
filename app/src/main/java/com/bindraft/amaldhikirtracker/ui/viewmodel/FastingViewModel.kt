package com.bindraft.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.util.FastingRules
import com.bindraft.amaldhikirtracker.util.HijriCalendar
import com.bindraft.amaldhikirtracker.util.LocationTracker
import com.bindraft.amaldhikirtracker.util.resolveSpiritualDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class StandardFastInfo(val type: FastingType, val nextOccurrence: FastingRules.NextOccurrence?)

data class RamadanSummary(val hijriYear: Int, val fasted: Int, val missed: Int, val remaining: Int, val totalDays: Int)

class FastingViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _spiritualDate = MutableStateFlow(LocalDate.now())
    val spiritualDate: StateFlow<LocalDate> = _spiritualDate.asStateFlow()

    private val _visibleHijriYear = MutableStateFlow(1447)
    private val _visibleHijriMonth = MutableStateFlow(1)

    /** Manual regional correction (see [FastingRules] doc) applied to every Hijri conversion below. */
    val hijriOffset: StateFlow<Int> = preferencesManager.hijriDateOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val visibleMonthLabel: StateFlow<String> = combine(_visibleHijriYear, _visibleHijriMonth) { year, month ->
        "${FastingRules.hijriMonthName(month)} $year"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** e.g. "September-October 2026" or "December 2026-January 2027" — the Gregorian span this Hijri month falls in. */
    val visibleGregorianLabel: StateFlow<String> = combine(_visibleHijriYear, _visibleHijriMonth, hijriOffset) { year, month, offset ->
        val start = HijriCalendar.toGregorian(year, month, 1, offset)
        val end = HijriCalendar.toGregorian(year, month, HijriCalendar.lengthOfMonth(year, month), offset)
        val startMonthName = start.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val endMonthName = end.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        when {
            start.year == end.year && start.month == end.month -> "$startMonthName ${start.year}"
            start.year == end.year -> "$startMonthName-$endMonthName ${start.year}"
            else -> "$startMonthName ${start.year}-$endMonthName ${end.year}"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val monthDates: StateFlow<List<LocalDate>> = combine(_visibleHijriYear, _visibleHijriMonth, hijriOffset) { year, month, offset ->
        val length = HijriCalendar.lengthOfMonth(year, month)
        (1..length).map { day -> HijriCalendar.toGregorian(year, month, day, offset) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthLogs: StateFlow<Map<LocalDate, FastingLog>> = combine(_visibleHijriYear, _visibleHijriMonth, hijriOffset) { y, m, o -> Triple(y, m, o) }
        .flatMapLatest { (year, month, offset) ->
            val start = HijriCalendar.toGregorian(year, month, 1, offset)
            val end = HijriCalendar.toGregorian(year, month, HijriCalendar.lengthOfMonth(year, month), offset)
            repository.getFastingLogsInRange(start, end)
        }
        .map { logs -> logs.associateBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allTypes: StateFlow<List<FastingType>> = repository.getAllFastingTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val standardFasts: StateFlow<List<StandardFastInfo>> = combine(allTypes, spiritualDate, hijriOffset) { types, date, offset ->
        types.filter { !it.isCustom }.map { StandardFastInfo(it, FastingRules.nextOccurrence(it.name, date, offset)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Missed/fasted/remaining count for Ramadan (mandatory fasting), only while that month is the one visible in the calendar. */
    val ramadanSummary: StateFlow<RamadanSummary?> = combine(
        _visibleHijriYear, _visibleHijriMonth, monthDates, monthLogs, spiritualDate
    ) { year, month, dates, logs, today ->
        if (month != 9 || dates.isEmpty()) return@combine null
        var fasted = 0
        var missed = 0
        var remaining = 0
        dates.forEach { date ->
            when {
                logs.containsKey(date) -> fasted++
                date.isBefore(today) -> missed++
                else -> remaining++
            }
        }
        RamadanSummary(year, fasted, missed, remaining, dates.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            val date = resolveSpiritualDate(preferencesManager, locationTracker)
            _spiritualDate.value = date
            val offset = preferencesManager.hijriDateOffset.first()
            val hijri = HijriCalendar.from(date, offset)
            _visibleHijriYear.value = hijri.year
            _visibleHijriMonth.value = hijri.month
        }
    }

    fun nextMonth() {
        val (year, month) = HijriCalendar.addMonths(_visibleHijriYear.value, _visibleHijriMonth.value, 1)
        _visibleHijriYear.value = year
        _visibleHijriMonth.value = month
    }

    fun previousMonth() {
        val (year, month) = HijriCalendar.addMonths(_visibleHijriYear.value, _visibleHijriMonth.value, -1)
        _visibleHijriYear.value = year
        _visibleHijriMonth.value = month
    }

    fun jumpToDate(date: LocalDate) {
        val hijri = HijriCalendar.from(date, hijriOffset.value)
        _visibleHijriYear.value = hijri.year
        _visibleHijriMonth.value = hijri.month
    }

    fun toggleDay(date: LocalDate, fastingTypeId: Long?) {
        if (FastingRules.isFastingForbidden(date, hijriOffset.value)) return
        if (date.isAfter(_spiritualDate.value)) return
        viewModelScope.launch {
            val existing = repository.getFastingLog(date)
            if (existing != null) {
                repository.deleteFastingLog(existing)
            } else {
                repository.insertFastingLog(FastingLog(date = date, fastingTypeId = fastingTypeId))
            }
        }
    }

    fun addFastingType(name: String, arabicName: String?) {
        viewModelScope.launch {
            repository.insertFastingType(FastingType(name = name, arabicName = arabicName, isCustom = true))
        }
    }

    fun deleteFastingType(fastingType: FastingType) {
        viewModelScope.launch {
            repository.deleteFastingType(fastingType)
        }
    }

    class Factory(
        private val repository: AmalRepository,
        private val preferencesManager: PreferencesManager,
        private val locationTracker: LocationTracker
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FastingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FastingViewModel(repository, preferencesManager, locationTracker) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
