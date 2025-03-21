package com.github.oryanmat.trellowidget.util

import androidx.room.TypeConverter
import com.github.oryanmat.trellowidget.data.model.Card
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object Json {
    private val gson = Gson()

    fun toJson(src: Any): String = gson.toJson(src)

    fun <T> fromJson(json: String, c: Class<T>): T = gson.fromJson(json, c)

    fun <T> tryParseJson(json: String, type: Type, defaultValue: T): T = try {
        gson.fromJson(json, type)
    } catch (e: JsonSyntaxException) {
        defaultValue
    }
}

class Converters {
    @TypeConverter
    fun fromCardList(value: List<Card>): String {
        return Json.toJson(value)
    }

    @TypeConverter
    fun toCardList(value: String): List<Card> {
        val type = object : TypeToken<List<Card>>() {}.type
        return Json.tryParseJson(value, type, emptyList())
    }
}