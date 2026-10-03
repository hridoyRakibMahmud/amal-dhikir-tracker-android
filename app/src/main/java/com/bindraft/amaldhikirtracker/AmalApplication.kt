package com.bindraft.amaldhikirtracker

import android.app.Application
import com.bindraft.amaldhikirtracker.data.local.AmalDatabase
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.data.sync.SyncManager
import com.bindraft.amaldhikirtracker.util.LocationTracker

class AmalApplication : Application() {
    val database by lazy { AmalDatabase.getDatabase(this) }
    val syncManager by lazy { SyncManager(database.amalDao()) }
    val repository by lazy { AmalRepository(database.amalDao(), syncManager) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val locationTracker by lazy { LocationTracker(this) }

    override fun onCreate() {
        super.onCreate()
        syncManager.attach()
    }
}
