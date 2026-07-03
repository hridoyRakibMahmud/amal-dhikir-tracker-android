package com.example.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.amaldhikirtracker.data.local.CalendarType
import com.example.amaldhikirtracker.data.local.PreferencesManager
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import com.example.amaldhikirtracker.data.repository.AmalRepository
import com.example.amaldhikirtracker.util.LocationTracker
import com.example.amaldhikirtracker.util.SunsetCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TrackerViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _spiritualDate = MutableStateFlow(LocalDate.now())
    val spiritualDate: StateFlow<LocalDate> = _spiritualDate.asStateFlow()

    val currentDate: StateFlow<LocalDate> = _spiritualDate // Compatibility

    val calendarType: StateFlow<CalendarType> = preferencesManager.calendarType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarType.HIJRI)

    val formattedDate: StateFlow<String> = combine(spiritualDate, calendarType) { date, type ->
        if (type == CalendarType.HIJRI) {
            val hijriDate = HijrahDate.from(date)
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
            hijriDate.format(formatter)
        } else {
            date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val todaySalatLogs: StateFlow<List<SalatLog>> = _spiritualDate
        .flatMapLatest { date -> repository.getSalatLogsByDate(date) }
        .map { logs ->
            // Sort by traditional order: Maghrib, Isha, Tahajjud, Fajr, Duha, Dhuhr, Asr
            val order = listOf("Maghrib", "Isha", "Tahajjud", "Fajr", "Duha", "Dhuhr", "Asr")
            logs.sortedBy { log ->
                val index = order.indexOf(log.salatName)
                if (index != -1) index else 99
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDhikirLogs: StateFlow<List<DhikirLog>> = _spiritualDate
        .flatMapLatest { date -> repository.getDhikirLogsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDhikirs: StateFlow<List<Dhikir>> = repository.getAllDhikirs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshSpiritualDate()
    }

    fun refreshSpiritualDate() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val location = locationTracker.getCurrentLocation()
            var sunset = LocalTime.of(18, 0) // Default

            if (location != null) {
                preferencesManager.updateLocation(location.latitude, location.longitude)
                sunset = SunsetCalculator.getSunsetTime(location.latitude, location.longitude, ZonedDateTime.now())
            } else {
                val lastLoc = preferencesManager.getLastLocation()
                if (lastLoc != null) {
                    sunset = SunsetCalculator.getSunsetTime(lastLoc.first, lastLoc.second, ZonedDateTime.now())
                }
            }

            val newSpiritualDate = if (now.toLocalTime().isAfter(sunset)) {
                now.toLocalDate().plusDays(1)
            } else {
                now.toLocalDate()
            }
            
            _spiritualDate.value = newSpiritualDate
            ensureInitialSalats()
        }
    }

    private fun ensureInitialSalats() {
        viewModelScope.launch {
            val fardSalats = listOf("Maghrib", "Isha", "Fajr", "Dhuhr", "Asr")
            val voluntarySalats = listOf("Tahajjud", "Duha")
            val targetDate = _spiritualDate.value
            
            fardSalats.forEach { name ->
                val existing = repository.getSalatLogByName(name, targetDate)
                if (existing == null) {
                    repository.insertSalatLog(
                        SalatLog(
                            salatName = name,
                            type = "FARD",
                            isCompleted = false,
                            date = targetDate
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
                            date = targetDate
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

    fun addVoluntarySalat(name: String) {
        viewModelScope.launch {
            repository.insertSalatLog(
                SalatLog(
                    salatName = name,
                    type = "VOLUNTARY",
                    isCompleted = true,
                    date = _spiritualDate.value
                )
            )
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
        private val locationTracker: LocationTracker
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrackerViewModel(repository, preferencesManager, locationTracker) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
