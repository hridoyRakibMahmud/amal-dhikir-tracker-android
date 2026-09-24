package com.bindraft.amaldhikirtracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

data class AuthProfile(val displayName: String, val email: String?, val photoUrl: String?)

class PreferencesManager(private val context: Context) {
    private val calendarTypeKey = stringPreferencesKey("calendar_type")
    private val lastLatKey = doublePreferencesKey("last_lat")
    private val lastLngKey = doublePreferencesKey("last_lng")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val authNameKey = stringPreferencesKey("auth_display_name")
    private val authEmailKey = stringPreferencesKey("auth_email")
    private val authPhotoKey = stringPreferencesKey("auth_photo_url")
    private val hijriDateOffsetKey = intPreferencesKey("hijri_date_offset")

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

    // Local session only — there is no backend yet, so nothing is synced. Set on successful
    // Google sign-in, cleared on sign-out.
    val authProfile: Flow<AuthProfile?> = context.dataStore.data
        .map { preferences ->
            val name = preferences[authNameKey] ?: return@map null
            AuthProfile(displayName = name, email = preferences[authEmailKey], photoUrl = preferences[authPhotoKey])
        }

    suspend fun setAuthProfile(profile: AuthProfile) {
        context.dataStore.edit { preferences ->
            preferences[authNameKey] = profile.displayName
            profile.email?.let { preferences[authEmailKey] = it } ?: preferences.remove(authEmailKey)
            profile.photoUrl?.let { preferences[authPhotoKey] = it } ?: preferences.remove(authPhotoKey)
        }
    }

    suspend fun clearAuthProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(authNameKey)
            preferences.remove(authEmailKey)
            preferences.remove(authPhotoKey)
        }
    }

    // Manual correction (in days, typically -2..+2) applied on top of the Umm al-Qura
    // calculation to match a specific region's local moon-sighting announcement.
    val hijriDateOffset: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[hijriDateOffsetKey] ?: 0 }

    suspend fun setHijriDateOffset(offsetDays: Int) {
        context.dataStore.edit { preferences ->
            preferences[hijriDateOffsetKey] = offsetDays
        }
    }
}
