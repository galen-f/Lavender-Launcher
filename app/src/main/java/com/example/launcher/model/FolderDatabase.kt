package com.example.launcher.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FolderEntity::class], version = 1)
abstract class FolderDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile private var instance: FolderDatabase? = null

        fun getDatabase(context: Context): FolderDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    FolderDatabase::class.java,
                    "launcher_database"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }
}
