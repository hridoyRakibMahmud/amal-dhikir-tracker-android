package com.example.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "salat_logs",
    indices = [Index(value = ["salatName", "date"], unique = true)]
)
data class SalatLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val salatName: String,
    val type: String, // FARD, VOLUNTARY
    val isCompleted: Boolean,
    val date: LocalDate
)
