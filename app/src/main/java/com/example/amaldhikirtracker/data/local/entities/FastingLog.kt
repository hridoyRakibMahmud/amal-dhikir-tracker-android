package com.example.amaldhikirtracker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "fasting_logs",
    indices = [Index(value = ["date"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = FastingType::class,
            parentColumns = ["id"],
            childColumns = ["fastingTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FastingLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val fastingTypeId: Long? = null
)
