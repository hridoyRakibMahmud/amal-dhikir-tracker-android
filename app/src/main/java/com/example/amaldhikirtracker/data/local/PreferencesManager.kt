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

class PreferencesManager(private val context: Context) {
    private val calendarTypeKey = stringPreferencesKey("calendar_type")
    private val lastLatKey = doublePreferencesKey("last_lat")
    private val lastLngKey = doublePreferencesKey("last_lng")

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
