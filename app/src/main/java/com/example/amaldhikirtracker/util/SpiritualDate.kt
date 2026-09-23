package com.example.amaldhikirtracker.util

import com.example.amaldhikirtracker.data.local.PreferencesManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * The Hijri/spiritual "today" — rolls over at Maghrib (sunset), not midnight.
 * Shared by any ViewModel that logs data against the spiritual day (Tracker, History, Counter).
 */
suspend fun resolveSpiritualDate(
    preferencesManager: PreferencesManager,
    locationTracker: LocationTracker
): LocalDate {
    val now = LocalDateTime.now()
    val location = locationTracker.getCurrentLocation()
    var sunset = LocalTime.of(18, 0)

    if (location != null) {
        preferencesManager.updateLocation(location.latitude, location.longitude)
        sunset = SunsetCalculator.getSunsetTime(location.latitude, location.longitude, ZonedDateTime.now())
    } else {
        val lastLoc = preferencesManager.getLastLocation()
        if (lastLoc != null) {
            sunset = SunsetCalculator.getSunsetTime(lastLoc.first, lastLoc.second, ZonedDateTime.now())
        }
    }

    return if (now.toLocalTime().isAfter(sunset)) now.toLocalDate().plusDays(1) else now.toLocalDate()
}
