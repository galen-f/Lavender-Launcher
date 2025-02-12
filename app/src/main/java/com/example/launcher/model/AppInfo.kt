package com.example.launcher.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppInfo(
    @PrimaryKey val packageName: String,
    val label: String,
    val folderIds: List<Int> = emptyList()
)