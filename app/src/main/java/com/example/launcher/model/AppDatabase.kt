package com.example.launcher.model

import android.content.Context
import androidx.room.*
import com.example.launcher.utils.BitmapConverter
import com.example.launcher.utils.IntListConverter

@Database(entities = [AppInfo::class], version = 2)
@TypeConverters(BitmapConverter::class, IntListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
