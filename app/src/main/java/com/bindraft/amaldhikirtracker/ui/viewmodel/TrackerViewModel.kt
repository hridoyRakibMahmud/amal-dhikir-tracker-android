package com.bindraft.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bindraft.amaldhikirtracker.data.local.CalendarType
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.util.HijriCalendar
import com.bindraft.amaldhikirtracker.util.LocationTracker
import com.bindraft.amaldhikirtracker.util.SpiritualDayProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val AL_KAHF_NAME = "Surah Al-Kahf"

class TrackerViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker,
    private val spiritualDay: SpiritualDayProvider
) : ViewModel() {

    val spiritualDate: StateFlow<LocalDate> = spiritualDay.date

    val currentDate: StateFlow<LocalDate> = spiritualDate // Compatibility

    val calendarType: StateFlow<CalendarType> = preferencesManager.calendarType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarType.HIJRI)

    val hijriOffset: StateFlow<Int> = preferencesManager.hijriDateOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val formattedDate: StateFlow<String> = combine(spiritualDate, calendarType, hijriOffset) { date, type, offset ->
        if (type == CalendarType.HIJRI) {
            val hijri = HijriCalendar.from(date, offset)
            val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            "$weekday, ${hijri.day} ${HijriCalendar.monthName(hijri.month)} ${hijri.year}"
        } else {
            date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // (primary, secondary) date strings for the Home header — primary follows the user's
    // preferred calendar type, secondary shows the other one for reference.
    val dateHeader: StateFlow<Pair<String, String>> = combine(spiritualDate, calendarType, hijriOffset) { date, type, offset ->
        val hijriDate = HijriCalendar.from(date, offset)
        val hijri = "${HijriCalendar.monthName(hijriDate.month)} ${hijriDate.day}, ${hijriDate.year} AH"
        val gregorian = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
        if (type == CalendarType.HIJRI) hijri to "$gregorian · Gregorian" else gregorian to "$hijri · Hijri"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "" to "")

    // Once the user picks a past day, stop following the spiritual-day rollover (same convention as Quran/Fasting).
    private var userSelectedDate = false

    private val _viewedDate = MutableStateFlow(spiritualDay.date.value)
    val viewedDate: StateFlow<LocalDate> = _viewedDate.asStateFlow()

    val isViewingToday: StateFlow<Boolean> = combine(viewedDate, spiritualDate) { viewed, today -> viewed == today }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun selectDate(date: LocalDate) {
        if (date.isAfter(spiritualDate.value)) return
        userSelectedDate = true
        _viewedDate.value = date
    }

    fun jumpToToday() {
        userSelectedDate = false
        _viewedDate.value = spiritualDate.value
    }

    val isViewedDateFriday: StateFlow<Boolean> = viewedDate.map { it.dayOfWeek == DayOfWeek.FRIDAY }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val viewedSalatLogs: StateFlow<List<SalatLog>> = viewedDate
        .flatMapLatest { date -> repository.getSalatLogsByDate(date) }
        .map { logs ->
            // Display order: Fard rows chronological (Fajr → Isha, Jumu'ah taking Dhuhr's slot on Friday), then Nafl rows.
            val order = listOf("Fajr", "Dhuhr", "Jumu'ah", "Asr", "Maghrib", "Isha", "Tahajjud", "Duha", AL_KAHF_NAME)
            logs.sortedBy { log ->
                val index = order.indexOf(log.salatName)
                if (index != -1) index else 99
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDhikirLogs: StateFlow<List<DhikirLog>> = spiritualDate
        .flatMapLatest { date -> repository.getDhikirLogsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDhikirs: StateFlow<List<Dhikir>> = repository.getAllDhikirs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The default "Durud" dhikir, highlighted in its own section on Friday. Null if it's been deleted. */
    val fridayDhikir: StateFlow<Dhikir?> = allDhikirs
        .map { dhikirs -> dhikirs.find { it.name.equals("Durud", ignoreCase = true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Dhikirs with a daily target that hasn't been reached yet today, paired with the count still needed. Backs the Dhikir reminder's popup. */
    val dhikirsWithRemainingTarget: StateFlow<List<Pair<Dhikir, Int>>> = combine(allDhikirs, todayDhikirLogs) { dhikirs, logs ->
        dhikirs.mapNotNull { dhikir ->
            val target = dhikir.dailyTarget ?: return@mapNotNull null
            val done = logs.filter { it.dhikirId == dhikir.id }.sumOf { it.count }
            val remaining = target - done
            if (remaining > 0) dhikir to remaining else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fardSalatLogs: StateFlow<List<SalatLog>> = viewedSalatLogs
        .map { logs -> logs.filter { it.type == "FARD" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val naflSalatLogs: StateFlow<List<SalatLog>> = viewedSalatLogs
        .map { logs -> logs.filter { it.type == "VOLUNTARY" && it.salatName != AL_KAHF_NAME } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Friday-only "Surah Al-Kahf" row, shown in its own section instead of alongside Nafl. */
    val fridaySalatLogs: StateFlow<List<SalatLog>> = viewedSalatLogs
        .map { logs -> logs.filter { it.salatName == AL_KAHF_NAME } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consecutive spiritual days (ending today) where all 5 Fard prayers were completed.
    val streak: StateFlow<Int> = spiritualDate
        .flatMapLatest { date ->
            repository.getSalatLogsInRange(date.minusDays(60), date).map { logs -> computeStreak(logs, date) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun computeStreak(logs: List<SalatLog>, endDate: LocalDate): Int {
        val fardNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val byDate = logs.groupBy { it.date }
        var count = 0
        var day = endDate
        while (true) {
            val dayLogs = byDate[day] ?: break
            val allDone = fardNames.all { name ->
                // Dhuhr's slot is logged as "Jumu'ah" on Friday.
                val namesToCheck = if (name == "Dhuhr") listOf("Dhuhr", "Jumu'ah") else listOf(name)
                namesToCheck.any { n -> dayLogs.any { it.salatName == n && it.isCompleted } }
            }
            if (!allDone) break
            count++
            day = day.minusDays(1)
        }
        return count
    }

    init {
        viewModelScope.launch {
            spiritualDay.ready.first { it }
            spiritualDate.collect { today ->
                if (!userSelectedDate) _viewedDate.value = today
                ensureInitialSalats(today)
            }
        }
        viewModelScope.launch {
            spiritualDay.ready.first { it }
            viewedDate.collect { date -> ensureInitialSalats(date) }
        }
    }

    private fun ensureInitialSalats(targetDate: LocalDate) {
        viewModelScope.launch {
            val isFriday = targetDate.dayOfWeek == DayOfWeek.FRIDAY
            val fardSalats = listOf("Maghrib", "Isha", "Fajr", "Dhuhr", "Asr")
            val fardArabic = mapOf(
                "Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر",
                "Maghrib" to "المغرب", "Isha" to "العشاء"
            )
            val voluntarySalats = listOf("Tahajjud", "Duha")
            val voluntaryIcons = mapOf("Tahajjud" to "bedtime", "Duha" to "wb_sunny")
            val voluntaryArabic = mapOf("Tahajjud" to "التهجد", "Duha" to "الضحى")

            fardSalats.forEach { name ->
                // Dhuhr's slot is Jumu'ah on Friday. If an older "Dhuhr" row already exists for
                // this Friday (seeded before this feature shipped), leave it rather than duplicate.
                val onFridayDhuhrSlot = name == "Dhuhr" && isFriday
                val seedName = if (onFridayDhuhrSlot) "Jumu'ah" else name
                val seedArabic = if (onFridayDhuhrSlot) "الجمعة" else fardArabic[name]
                val existing = repository.getSalatLogByName(seedName, targetDate)
                    ?: if (onFridayDhuhrSlot) repository.getSalatLogByName("Dhuhr", targetDate) else null
                if (existing == null) {
                    repository.insertSalatLog(
                        SalatLog(
                            salatName = seedName,
                            type = "FARD",
                            isCompleted = false,
                            date = targetDate,
                            arabicName = seedArabic
                        )
                    )
                }
            }

            voluntarySalats.forEach { name ->
                val existing = repository.getSalatLogByName(name, targetDate)
                if (existing == null) {
                    repository.insertSalatLog(
                        SalatLog(
                            salatName = name,
                            type = "VOLUNTARY",
                            isCompleted = false,
                            date = targetDate,
                            icon = voluntaryIcons[name],
                            arabicName = voluntaryArabic[name]
                        )
                    )
                }
            }

            if (isFriday) {
                val existing = repository.getSalatLogByName(AL_KAHF_NAME, targetDate)
                if (existing == null) {
                    repository.insertSalatLog(
                        SalatLog(
                            salatName = AL_KAHF_NAME,
                            type = "VOLUNTARY",
                            isCompleted = false,
                            date = targetDate,
                            arabicName = "سورة الكهف"
                        )
                    )
                }
            }
        }
    }

    fun toggleSalat(salatLog: SalatLog) {
        viewModelScope.launch {
            repository.insertSalatLog(salatLog.copy(isCompleted = !salatLog.isCompleted))
        }
    }

    fun toggleSunnah(salatLog: SalatLog) {
        viewModelScope.launch {
            repository.insertSalatLog(salatLog.copy(sunnahDone = !salatLog.sunnahDone))
        }
    }

    fun addVoluntarySalat(name: String, icon: String? = null) {
        viewModelScope.launch {
            repository.insertSalatLog(
                SalatLog(
                    salatName = name,
                    type = "VOLUNTARY",
                    isCompleted = false,
                    date = viewedDate.value,
                    icon = icon
                )
            )
        }
    }

    fun setDhikirTarget(dhikirId: Long, dailyTarget: Int?) {
        viewModelScope.launch {
            repository.setDhikirTarget(dhikirId, dailyTarget)
        }
    }

    fun addDhikir(name: String, arabicName: String?, category: String?) {
        viewModelScope.launch {
            repository.insertDhikir(
                Dhikir(name = name, arabicName = arabicName, category = category, isCustom = true)
            )
        }
    }

    fun updateDhikir(dhikir: Dhikir) {
        viewModelScope.launch {
            repository.updateDhikir(dhikir)
        }
    }

    fun deleteDhikir(dhikir: Dhikir) {
        viewModelScope.launch {
            repository.deleteDhikir(dhikir)
        }
    }

    class Factory(
        private val repository: AmalRepository,
        private val preferencesManager: PreferencesManager,
        private val locationTracker: LocationTracker,
        private val spiritualDay: SpiritualDayProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrackerViewModel(repository, preferencesManager, locationTracker, spiritualDay) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
