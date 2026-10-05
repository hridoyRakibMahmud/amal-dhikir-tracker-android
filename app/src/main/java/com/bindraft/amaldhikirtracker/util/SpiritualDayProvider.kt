package com.bindraft.amaldhikirtracker.util

import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Single source of the current spiritual day (rolls over at Maghrib). Recomputes it and sleeps
 * until the next sunset, so the day changes while the app is open. Call [onForeground] when the
 * app returns to the foreground, since timers don't run while it's backgrounded or the phone sleeps.
 */
class SpiritualDayProvider(
    private val preferencesManager: PreferencesManager,
    private val locationTracker: LocationTracker,
    private val scope: CoroutineScope
) {
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    // False until the first real calculation, so callers don't act on the placeholder date.
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private var loop: Job? = null

    fun start() {
        loop?.cancel()
        loop = scope.launch {
            while (true) {
                _date.value = resolveSpiritualDate(preferencesManager, locationTracker)
                _ready.value = true
                val now = ZonedDateTime.now()
                val rollover = nextRollover(now)
                delay(Duration.between(now, rollover).toMillis() + ROLLOVER_MARGIN_MS)
            }
        }
    }

    fun onForeground() = start()

    private suspend fun nextRollover(now: ZonedDateTime): ZonedDateTime {
        val lastLocation = preferencesManager.getLastLocation()
        fun sunsetOn(day: ZonedDateTime): ZonedDateTime {
            val time = lastLocation?.let { SunsetCalculator.getSunsetTime(it.first, it.second, day) }
                ?: LocalTime.of(18, 0)
            return day.toLocalDate().atTime(time).atZone(day.zone)
        }
        val today = sunsetOn(now)
        return if (now.isBefore(today)) today else sunsetOn(now.plusDays(1))
    }

    private companion object {
        // Wake just after sunset so the day comparison in resolveSpiritualDate flips.
        const val ROLLOVER_MARGIN_MS = 2_000L
    }
}
