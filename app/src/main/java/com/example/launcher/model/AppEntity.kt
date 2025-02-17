package com.example.launcher.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(                       // Does not include app icon as this is performance deficient
    @PrimaryKey val packageName: String,    // Technical app name (used to launch the app)
    val label: String,                      // Humane readable app name (Used to display the app)
    val isDockApp: Boolean = false          // boolean which tracks which apps are docked
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // Unique ID for the folders
    val name: String                                    // Folder name (User generated, not necessarily unique, used for display)
)

@Entity( //Junction table
    tableName = "app_folders",
    primaryKeys = ["packageName", "folderId"],
    foreignKeys = [
        ForeignKey(entity = AppEntity::class, parentColumns = ["packageName"], childColumns = ["packageName"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["folderId"]), Index(value = ["packageName"])]
)
data class AppFolderEntity(
    val folderId: Int,          // FK to folderEntity
    val packageName: String     // FK to AppEntity
)