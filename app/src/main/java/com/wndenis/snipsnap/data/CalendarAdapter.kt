package com.wndenis.snipsnap.data

import android.util.Log
import androidx.compose.runtime.saveable.Saver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.wndenis.snipsnap.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.time.LocalDateTime

const val SECTION_COUNT = 9

data class CalendarAdapter(
    val name: String,
    val sections: List<CalendarSection> = (0 until SECTION_COUNT).map { CalendarSection() }
) {

    fun exportToString(): String {
        return gson.toJson(this)
    }

    fun exportToFile() {
        val jsonRepr = exportToString()
        val folder = MainActivity.getContext().filesDir
        val filename = "$name.spsp"
        // withContext(Dispatchers.IO) {
        val file = File(folder, filename)
        if (file.exists()) {
            file.delete()
        }
        val res = file.writeText(jsonRepr)
        Log.i("[SAVE]", "Save $res ${file.absolutePath}")
        // }
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
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            // val job = scope.async {
            var fileContent = ""
            val folder = MainActivity.getContext().filesDir
            val file = File(folder, filename)
            if (file.exists())
                fileContent = file.readText()
            // return@async fileContent
            // }
            // val fileContent = job.await()
            if (fileContent == "")
                return null
            return importFromString(fileContent)
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
