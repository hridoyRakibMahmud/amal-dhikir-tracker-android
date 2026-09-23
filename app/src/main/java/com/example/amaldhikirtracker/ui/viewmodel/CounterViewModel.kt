package com.example.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.amaldhikirtracker.data.local.PreferencesManager
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.repository.AmalRepository
import com.example.amaldhikirtracker.util.LocationTracker
import com.example.amaldhikirtracker.util.resolveSpiritualDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CounterViewModel(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker,
    private val dhikirId: Long
) : ViewModel() {

    private val _spiritualDate = MutableStateFlow(LocalDate.now())
    val spiritualDate: StateFlow<LocalDate> = _spiritualDate.asStateFlow()

    private val _dhikir = MutableStateFlow<Dhikir?>(null)
    val dhikir: StateFlow<Dhikir?> = _dhikir.asStateFlow()

    // Displayed running count for this session (taps + manual adds).
    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    // Tap-only portion not yet written to the DB (manual adds write through immediately,
    // so they must not be double-counted when the tap portion is flushed on save/reset).
    private var unpersistedTaps = 0

    val totalToday: StateFlow<Int> = _spiritualDate
        .flatMapLatest { date -> repository.getDhikirLogsByDate(date) }
        .map { logs -> logs.find { it.dhikirId == dhikirId }?.count ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            _dhikir.value = repository.getDhikirById(dhikirId)
            _spiritualDate.value = resolveSpiritualDate(preferencesManager, locationTracker)
        }
    }

    fun increment() {
        _sessionCount.value++
        unpersistedTaps++
    }

    fun resetSession() {
        _sessionCount.value = 0
        unpersistedTaps = 0
    }

    /** Adds a count logged outside the app — writes through immediately (session/today/total). */
    fun addManual(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.upsertDhikirLog(
                DhikirLog(dhikirId = dhikirId, count = amount, date = _spiritualDate.value)
            )
            _sessionCount.value += amount
        }
    }

    fun setTarget(target: Int?) {
        viewModelScope.launch {
            repository.setDhikirTarget(dhikirId, target)
            _dhikir.value = repository.getDhikirById(dhikirId)
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            if (unpersistedTaps > 0) {
                repository.upsertDhikirLog(
                    DhikirLog(dhikirId = dhikirId, count = unpersistedTaps, date = _spiritualDate.value)
                )
            }
            _sessionCount.value = 0
            unpersistedTaps = 0
        }
    }

    class Factory(
        private val repository: AmalRepository,
        private val preferencesManager: PreferencesManager,
        private val locationTracker: LocationTracker,
        private val dhikirId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CounterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CounterViewModel(repository, preferencesManager, locationTracker, dhikirId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
