package com.example.launcher.utils

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

// This is the file which allows for the use of app icons in a jetpack friendly manner, room for improvement but functional.
object AppIconUtils {
    fun getAppIconBitmap(packageManager: PackageManager, packageName: String): Bitmap? {
        return try {
            // Package manager returns a drawable, to use a drawable you need to use some complex libraries like the partially deprecated Accompanist lib, that would be faster but kept causing issues, a bitmap can be used in jetpack so ive just converted it. Slightly inefficient, but works.
            val drawable = packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(100, 100) // Bitmap remembered size (resolution)
        } catch (e: PackageManager.NameNotFoundException) {
            // If the icon cannot be found, display no icon
            // TODO: Add placeholder image, not super important since this will basically never happen
            null
        }
    }
}