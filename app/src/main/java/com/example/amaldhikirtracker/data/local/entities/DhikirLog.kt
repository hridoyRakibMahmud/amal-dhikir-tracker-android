package com.example.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "dhikir_logs",
    indices = [Index(value = ["dhikirId", "date"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Dhikir::class,
            parentColumns = ["id"],
            childColumns = ["dhikirId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DhikirLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dhikirId: Long,
    val count: Int,
    val date: LocalDate
)
