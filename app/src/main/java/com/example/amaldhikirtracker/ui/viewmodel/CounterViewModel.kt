package com.example.amaldhikirtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.repository.AmalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CounterViewModel(
    private val repository: AmalRepository,
    private val dhikirId: Long
) : ViewModel() {

    private val _dhikir = MutableStateFlow<Dhikir?>(null)
    val dhikir: StateFlow<Dhikir?> = _dhikir.asStateFlow()

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    private val _totalToday = MutableStateFlow(0)
    val totalToday: StateFlow<Int> = _totalToday.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _dhikir.value = repository.getDhikirById(dhikirId)
            repository.getDhikirLogsByDate(LocalDate.now()).collect { logs ->
                val log = logs.find { it.dhikirId == dhikirId }
                _totalToday.value = log?.count ?: 0
            }
        }
    }

    fun increment() {
        _sessionCount.value++
    }

    fun resetSession() {
        _sessionCount.value = 0
    }

    fun updateCount(newTotal: Int) {
        viewModelScope.launch {
            repository.insertDhikirLog(
                DhikirLog(
                    dhikirId = dhikirId,
                    count = newTotal,
                    date = LocalDate.now()
                )
            )
            // The flow in loadData will update _totalToday
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            if (_sessionCount.value > 0) {
                repository.upsertDhikirLog(
                    DhikirLog(
                        dhikirId = dhikirId,
                        count = _sessionCount.value,
                        date = LocalDate.now()
                    )
                )
                _sessionCount.value = 0
            }
        }
    }

    class Factory(
        private val repository: AmalRepository,
        private val dhikirId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CounterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CounterViewModel(repository, dhikirId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
