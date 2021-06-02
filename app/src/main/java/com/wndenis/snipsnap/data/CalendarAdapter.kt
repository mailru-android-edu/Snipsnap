package com.wndenis.snipsnap.data

import android.util.Log
import androidx.compose.runtime.saveable.Saver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.wndenis.snipsnap.MainActivity
import java.io.File
import java.time.LocalDateTime

data class CalendarAdapter(
    val name: String,
    val sections: List<CalendarSection> = (0..25).map { CalendarSection() }
) {

    fun exportToString(): String {
        return gson.toJson(this)
    }

    fun exportToFile() {
        val jsonRepr = exportToString()
        val folder = MainActivity.getContext().filesDir
        val filename = "$name.spsp"
        val file = File(folder, filename)
        if (file.exists()) {
            file.delete()
        }
        val res = file.writeText(jsonRepr)
        Log.i("[SAVE]", "Save $res ${file.absolutePath}")
    }

    companion object {
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(
                LocalDateTime::class.java,
                LocalDateTimeAdapter().nullSafe()
            ).create()

        fun importFromString(stringRepr: String): CalendarAdapter? {
            return gson.fromJson(stringRepr, CalendarAdapter::class.java)
        }

        fun importFromFile(filename: String): CalendarAdapter? {
            val folder = MainActivity.getContext().filesDir
            val file = File(folder, filename)
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

private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(jsonWriter: JsonWriter, localDate: LocalDateTime) {
        jsonWriter.value(localDate.toString())
    }

    override fun read(jsonReader: JsonReader): LocalDateTime {
        return LocalDateTime.parse(jsonReader.nextString())
    }
}
