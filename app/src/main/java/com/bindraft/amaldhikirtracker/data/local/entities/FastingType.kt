package com.bindraft.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_types")
data class FastingType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val arabicName: String? = null,
    val isCustom: Boolean = false
)
