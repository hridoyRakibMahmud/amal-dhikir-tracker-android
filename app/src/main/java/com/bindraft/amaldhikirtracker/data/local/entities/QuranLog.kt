package com.bindraft.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "quran_logs",
    indices = [Index(value = ["quranModeId", "date"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = QuranMode::class,
            parentColumns = ["id"],
            childColumns = ["quranModeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuranLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = java.util.UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val quranModeId: Long,
    val ayahsRead: Int,
    val date: LocalDate
)
