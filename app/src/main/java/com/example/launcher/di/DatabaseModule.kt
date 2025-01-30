package com.example.launcher.di

import android.content.Context
import androidx.room.Room
import com.example.launcher.model.FolderDao
import com.example.launcher.model.FolderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Ensures dependencies live as long as the app
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FolderDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FolderDatabase::class.java,
            "launcher_database"
        ).build()
    }

    @Provides
    fun provideFolderDao(database: FolderDatabase): FolderDao {
        return database.folderDao()
    }
}
