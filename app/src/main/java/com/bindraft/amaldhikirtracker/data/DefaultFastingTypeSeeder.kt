package com.bindraft.amaldhikirtracker.data

import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.repository.AmalRepository
import kotlinx.coroutines.flow.first

/**
 * Seeds the five standard Sunnah fasts once per install. Fixed syncIds keep the same type
 * from duplicating across devices. Existing installs have these types with random syncIds,
 * so they're matched by name and migrated to the fixed syncIds.
 */
class DefaultFastingTypeSeeder(
    private val repository: AmalRepository,
    private val preferencesManager: PreferencesManager
) {

    suspend fun seed() {
        if (preferencesManager.getDefaultFastingSeedVersion() >= SEED_VERSION) return

        val existing = repository.getAllFastingTypes().first()
        for (default in DEFAULTS) {
            val match = existing.find { it.name.equals(default.name, ignoreCase = true) }
            when {
                match == null -> repository.insertFastingType(
                    FastingType(
                        syncId = default.syncId,
                        name = default.name,
                        arabicName = default.arabicName,
                        isCustom = false
                    )
                )
                match.syncId != default.syncId -> repository.changeFastingTypeSyncId(match, default.syncId)
            }
        }

        preferencesManager.setDefaultFastingSeedVersion(SEED_VERSION)
    }

    private data class DefaultFasting(val slug: String, val name: String, val arabicName: String) {
        val syncId: String get() = "default_fasting_$slug"
    }

    private companion object {
        const val SEED_VERSION = 1

        val DEFAULTS = listOf(
            DefaultFasting("mondays-thursdays", "Mondays & Thursdays", "الإثنين والخميس"),
            DefaultFasting("ayyam-al-bidh", "Ayyam al-Bidh (White Days)", "أيام البيض"),
            DefaultFasting("ashura", "Ashura", "عاشوراء"),
            DefaultFasting("arafah", "Arafah", "عرفة"),
            DefaultFasting("six-of-shawwal", "Six of Shawwal", "ستة من شوال")
        )
    }
}
