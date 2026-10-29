package com.bindraft.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_modes")
data class QuranMode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String,
    val isCustom: Boolean = false,
    val dailyTarget: Int? = null
)
