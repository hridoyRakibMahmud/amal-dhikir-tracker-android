package com.example.amaldhikirtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.amaldhikirtracker.data.local.converters.DateConverters
import com.example.amaldhikirtracker.data.local.dao.AmalDao
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SalatLog::class, Dhikir::class, DhikirLog::class],
    version = 3,
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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Instance?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.amalDao()
                                    val initialDhikirs = listOf(
                                        Dhikir(name = "SubhanAllah", arabicName = "سُبْحَانَ ٱللَّٰهِ", category = "Daily"),
                                        Dhikir(name = "Alhamdulillah", arabicName = "ٱلْحَمْدُ لِلَّٰهِ", category = "Daily"),
                                        Dhikir(name = "Allahu Akbar", arabicName = "ٱللَّٰهُ أَكْبَرُ", category = "Daily"),
                                        Dhikir(name = "La ilaha illallah", arabicName = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", category = "Daily")
                                    )
                                    initialDhikirs.forEach { dao.insertDhikir(it) }
                                }
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
