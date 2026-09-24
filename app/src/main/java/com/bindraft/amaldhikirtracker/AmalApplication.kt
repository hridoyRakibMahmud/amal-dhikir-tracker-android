package com.bindraft.amaldhikirtracker

import android.app.Application
import com.bindraft.amaldhikirtracker.data.local.AmalDatabase
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.util.LocationTracker

class AmalApplication : Application() {
    val database by lazy { AmalDatabase.getDatabase(this) }
    val repository by lazy { AmalRepository(database.amalDao()) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val locationTracker by lazy { LocationTracker(this) }
}
