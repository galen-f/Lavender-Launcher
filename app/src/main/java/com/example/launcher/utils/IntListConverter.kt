package com.example.launcher.utils

import androidx.room.TypeConverter

object IntListConverter {

    // Room cannot store lists, which is needed to allow the app to be in multiple folders at once
    // This converts the list into a sort of CSV looking string.
    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(data: String?): List<Int> {
        return data?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }
}
