package com.example.amaldhikirtracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class CalendarType {
    GREGORIAN, HIJRI
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class PreferencesManager(private val context: Context) {
    private val calendarTypeKey = stringPreferencesKey("calendar_type")
    private val lastLatKey = doublePreferencesKey("last_lat")
    private val lastLngKey = doublePreferencesKey("last_lng")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val calendarType: Flow<CalendarType> = context.dataStore.data
        .map { preferences ->
            val type = preferences[calendarTypeKey] ?: CalendarType.HIJRI.name
            CalendarType.valueOf(type)
        }

    suspend fun setCalendarType(type: CalendarType) {
        context.dataStore.edit { preferences ->
            preferences[calendarTypeKey] = type.name
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val mode = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(mode)
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }

    suspend fun updateLocation(lat: Double, lng: Double) {
        context.dataStore.edit { preferences ->
            preferences[lastLatKey] = lat
            preferences[lastLngKey] = lng
        }
    }

    suspend fun getLastLocation(): Pair<Double, Double>? {
        val prefs = context.dataStore.data.first()
        val lat = prefs[lastLatKey] ?: return null
        val lng = prefs[lastLngKey] ?: return null
        return Pair(lat, lng)
    }
}
