package com.bindraft.amaldhikirtracker.data

import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import kotlinx.coroutines.flow.first

/**
 * Adds the default daily dhikirs (target 100) for every install, once per seed version.
 * Existing rows are matched by name and left alone, except a missing target is set to the default.
 * Writes go through the repository so they sync. New defaults use fixed syncIds, so the same
 * dhikir seeded on two devices merges instead of duplicating.
 *
 * Seed version 2 fixes a mistake in version 1: "Subhanallahi wa bihamdihi" and "Subhanallahi
 * Adhim" were seeded as two separate dhikirs, but they're one combined dhikir said together.
 * Installs that already have both get them merged (logged counts summed day-by-day) into one.
 */
class DefaultDhikirSeeder(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager
) {

    suspend fun seed() {
        if (preferencesManager.getDefaultDhikirSeedVersion() >= SEED_VERSION) return

        mergeSubhanallahiPair()

        val existing = repository.getAllDhikirs().first()
        for (default in DEFAULTS) {
            val match = existing.find { it.name.equals(default.name, ignoreCase = true) }
            when {
                match == null -> repository.insertDhikir(
                    Dhikir(
                        syncId = default.syncId,
                        name = default.name,
                        arabicName = default.arabicName,
                        category = "Daily",
                        isCustom = false,
                        dailyTarget = DEFAULT_TARGET
                    )
                )
                match.dailyTarget == null -> repository.setDhikirTarget(match.id, DEFAULT_TARGET)
            }
        }

        preferencesManager.setDefaultDhikirSeedVersion(SEED_VERSION)
    }

    private suspend fun mergeSubhanallahiPair() {
        val existing = repository.getAllDhikirs().first()
        val wa = existing.find { it.name.equals("Subhanallahi wa bihamdihi", ignoreCase = true) }
        val adhim = existing.find { it.name.equals("Subhanallahi Adhim", ignoreCase = true) }
        when {
            wa != null && adhim != null -> {
                repository.mergeDhikirLogsInto(primaryId = wa.id, duplicateId = adhim.id)
                repository.deleteDhikir(adhim)
                repository.changeDhikirSyncId(wa, newSyncId = COMBINED.syncId, newName = COMBINED.name)
            }
            wa != null -> repository.changeDhikirSyncId(wa, newSyncId = COMBINED.syncId, newName = COMBINED.name)
            adhim != null -> repository.changeDhikirSyncId(adhim, newSyncId = COMBINED.syncId, newName = COMBINED.name)
            else -> { /* Neither exists yet — the DEFAULTS loop below inserts the combined one fresh. */ }
        }
    }

    private data class DefaultDhikir(val slug: String, val name: String, val arabicName: String) {
        val syncId: String get() = "default_$slug"
    }

    private companion object {
        const val SEED_VERSION = 2
        const val DEFAULT_TARGET = 100

        val COMBINED = DefaultDhikir(
            "subhanallahi-wa-bihamdihi-adhim",
            "Subhanallahi wa bihamdihi, Subhanallahi Adhim",
            "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ سُبْحَانَ ٱللَّٰهِ ٱلْعَظِيمِ"
        )

        // The first four already exist on installs from before this seeder, so their names
        // are kept as-is and matched by name.
        val DEFAULTS = listOf(
            DefaultDhikir("astaghfirullah", "Astaghfirullah", "أَسْتَغْفِرُ ٱللَّٰهَ"),
            DefaultDhikir("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰهِ"),
            DefaultDhikir("alhamdulillah", "Alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ"),
            DefaultDhikir("la-ilaha-illallah", "La ilaha illallah", "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ"),
            DefaultDhikir("allahu-akbar", "Allahu Akbar", "ٱللَّٰهُ أَكْبَرُ"),
            COMBINED,
            DefaultDhikir("durud", "Durud", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ")
        )
    }
}
