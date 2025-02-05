package com.example.launcher.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppInfo(
    @PrimaryKey val packageName: String,
    val label: String,
    val icon: ByteArray?,
    val folderIds: List<Int> = emptyList() // Tracks folders an app belongs to
)