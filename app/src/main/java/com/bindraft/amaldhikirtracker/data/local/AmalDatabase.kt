package com.bindraft.amaldhikirtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bindraft.amaldhikirtracker.data.local.converters.DateConverters
import com.bindraft.amaldhikirtracker.data.local.dao.AmalDao
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog

@Database(
    entities = [SalatLog::class, Dhikir::class, DhikirLog::class, FastingType::class, FastingLog::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AmalDatabase : RoomDatabase() {
    abstract fun amalDao(): AmalDao

    companion object {
        @Volatile
        private var Instance: AmalDatabase? = null

        fun getDatabase(context: Context): AmalDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AmalDatabase::class.java, "amal_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
