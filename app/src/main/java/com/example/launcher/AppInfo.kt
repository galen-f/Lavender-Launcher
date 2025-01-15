package com.example.launcher

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Bitmap
)