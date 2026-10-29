package com.bindraft.amaldhikirtracker.data

import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.QuranMode
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import kotlinx.coroutines.flow.first

/**
 * Seeds the four Quran reading modes once per install (and re-applies renames on a seed-version
 * bump, matched by syncId so existing logs stay attached). Fixed syncIds keep them from
 * duplicating across devices (same lesson as the fasting-type and dhikir default seeders).
 */
class DefaultQuranModeSeeder(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager
) {

    suspend fun seed() {
        if (preferencesManager.getDefaultQuranModeSeedVersion() >= SEED_VERSION) return

        val existing = repository.getAllQuranModes().first()
        for (default in DEFAULTS) {
            val match = existing.find { it.syncId == default.syncId }
            when {
                match == null -> repository.insertQuranMode(
                    QuranMode(syncId = default.syncId, name = default.name, isCustom = false)
                )
                match.name != default.name -> repository.insertQuranMode(match.copy(name = default.name))
            }
        }

        preferencesManager.setDefaultQuranModeSeedVersion(SEED_VERSION)
    }

    private data class DefaultMode(val slug: String, val name: String) {
        val syncId: String get() = "default_quran_$slug"
    }

    private companion object {
        const val SEED_VERSION = 2

        val DEFAULTS = listOf(
            DefaultMode("read", "Arabic"),
            DefaultMode("meaning", "Arabic with Meaning"),
            DefaultMode("only-meaning", "Only Meaning"),
            DefaultMode("tafsir", "With Tafsir")
        )
    }
}
