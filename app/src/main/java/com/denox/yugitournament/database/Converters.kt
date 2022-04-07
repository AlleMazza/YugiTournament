package com.denox.yugitournament.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

object Converters {

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType: Type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType: Type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType: Type = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromLongList(list: List<Long>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

}
