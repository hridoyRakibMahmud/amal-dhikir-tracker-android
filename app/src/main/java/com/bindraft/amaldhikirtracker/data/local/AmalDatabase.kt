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
import com.bindraft.amaldhikirtracker.data.local.entities.QuranLog
import com.bindraft.amaldhikirtracker.data.local.entities.QuranMode
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog

@Database(
    entities = [SalatLog::class, Dhikir::class, DhikirLog::class, FastingType::class, FastingLog::class, QuranMode::class, QuranLog::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverters::class)
abstract class AmalDatabase : RoomDatabase() {
    abstract fun amalDao(): AmalDao

    companion object {
        @Volatile
        private var Instance: AmalDatabase? = null

        // No migrations exist yet because the schema hasn't changed since exportSchema was
        // turned on. The next version bump MUST ship a real Migration added below via
        // .addMigrations(...) — without one, Room throws instead of silently wiping user data,
        // which is the point: a loud crash in testing beats a silent data loss in production.
        fun getDatabase(context: Context): AmalDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AmalDatabase::class.java, "amal_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
