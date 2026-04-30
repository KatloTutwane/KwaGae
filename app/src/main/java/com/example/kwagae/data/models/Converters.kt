package com.example.kwagae.data.converters

import androidx.room.TypeConverter

class Converters {

    /** Store amenities as a comma-separated string, restore as List<String> */
    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        list?.joinToString(",") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").map { it.trim() }
}