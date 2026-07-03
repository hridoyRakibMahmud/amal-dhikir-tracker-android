package com.example.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikirs")
data class Dhikir(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val arabicName: String? = null,
    val category: String? = null,
    val isCustom: Boolean = false
)
