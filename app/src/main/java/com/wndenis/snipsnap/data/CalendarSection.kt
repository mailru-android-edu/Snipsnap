package com.wndenis.snipsnap.data

import com.google.gson.annotations.SerializedName


data class CalendarSection(
    // ignore name to do not export empty unused strings
    @Transient val name: String = "",
    @SerializedName("events") val events: MutableList<CalendarEvent> = mutableListOf()
) {
    fun gc() {
        val toRemove = mutableListOf<Int>()
        events.withIndex().forEach { v -> if (v.value.deleted) toRemove.add(v.index) }
        for (i in toRemove.reversed()) {
            events.removeAt(i)
        }
    }
}