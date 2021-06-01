package com.wndenis.snipsnap.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

data class CalendarAdapter(
    val name: String,
    val sections: List<CalendarSection> = (0..25).map { CalendarSection() }
) {

    fun exportToString(): String {
        return gson.toJson(this)
    }

    fun exportToFile() {
        val jsonRepr = exportToString()
        val filename = "$name.spsp"
        val file = File(filename)
        if (file.exists()) {
            file.delete()
        }
        file.writeText(jsonRepr)
    }

    companion object {
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateAdapter().nullSafe()).create()

        fun importFromString(stringRepr: String): CalendarAdapter? {
            return Gson().fromJson(stringRepr, CalendarAdapter::class.java)
        }

        fun importFromFile(filename: String): CalendarAdapter? {
            val file = File(filename)
            if (!file.exists())
                return null
            val jsonRepr = file.readText()
            return importFromString(jsonRepr)
        }
        val AdapterSaver = run {
            Saver<CalendarAdapter, String>(
                save = { gson.toJson(it) },
                restore = { importFromString(it) }
            )
        }
    }
}


private class LocalDateAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(jsonWriter: JsonWriter, localDate: LocalDateTime) {
        jsonWriter.value(localDate.toString())
    }

    override fun read(jsonReader: JsonReader): LocalDateTime {
        return LocalDateTime.parse(jsonReader.nextString())
    }
}