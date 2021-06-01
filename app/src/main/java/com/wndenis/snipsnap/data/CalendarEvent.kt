package com.wndenis.snipsnap.data

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import com.wndenis.snipsnap.ui.theme.G500
import java.time.LocalDateTime

data class CalendarEvent(
    @SerializedName("startDate") var startDate: LocalDateTime,
    @SerializedName("endDate") var endDate: LocalDateTime,
    @SerializedName("name") var name: String = "",
    @SerializedName("color") var color: Color = G500,
    @SerializedName("deleted") var deleted: Boolean = false
) {
    fun mimic(calendarEvent: CalendarEvent) {
        this.startDate = calendarEvent.startDate
        this.endDate = calendarEvent.endDate
        this.name = calendarEvent.name
        this.color = calendarEvent.color
        this.deleted = calendarEvent.deleted
    }
}