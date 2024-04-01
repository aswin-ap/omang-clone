package com.omang.app.utils.dbConvertors

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omang.app.data.model.resources.OptionItem
import com.omang.app.data.model.test.AttemptedQuestion
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return list?.joinToString(",") // Convert List<Int> to comma-separated string
    }

    @TypeConverter
    fun toList(string: String?): List<Int>? {
        return string?.split(",")?.mapNotNull { it.toIntOrNull() } // Convert back to List<Int>
    }

    @TypeConverter
    fun fromOptionsList(value: List<OptionItem>): String {
        val gson = Gson()
        val type = object : TypeToken<List<OptionItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toOptionsList(value: String): List<OptionItem> {
        val gson = Gson()
        val type = object : TypeToken<List<OptionItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromQuestionsList(value: List<AttemptedQuestion>): String {
        val gson = Gson()
        val type = object : TypeToken<List<AttemptedQuestion>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toQuestionsList(value: String): List<AttemptedQuestion> {
        val gson = Gson()
        val type = object : TypeToken<List<AttemptedQuestion>>() {}.type
        return gson.fromJson(value, type)
    }
}
