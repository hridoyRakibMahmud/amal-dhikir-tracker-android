package com.bindraft.amaldhikirtracker

import android.app.Application
import com.bindraft.amaldhikirtracker.data.DefaultDhikirSeeder
import com.bindraft.amaldhikirtracker.data.DefaultFastingTypeSeeder
import com.bindraft.amaldhikirtracker.data.local.AmalDatabase
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import com.bindraft.amaldhikirtracker.data.sync.SyncManager
import com.bindraft.amaldhikirtracker.util.LocationTracker
import com.bindraft.amaldhikirtracker.util.SpiritualDayProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AmalApplication : Application() {
    val database by lazy { AmalDatabase.getDatabase(this) }
    val syncManager by lazy { SyncManager(database.amalDao()) }
    val repository by lazy { AmalRepository(database.amalDao(), syncManager) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val locationTracker by lazy { LocationTracker(this) }
    private val appScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    val spiritualDay by lazy { SpiritualDayProvider(preferencesManager, locationTracker, appScope) }

    override fun onCreate() {
        super.onCreate()
        syncManager.attach()
        spiritualDay.start()
        appScope.launch {
            DefaultDhikirSeeder(repository, preferencesManager).seed()
            DefaultFastingTypeSeeder(repository, preferencesManager).seed()
        }
    }
}
