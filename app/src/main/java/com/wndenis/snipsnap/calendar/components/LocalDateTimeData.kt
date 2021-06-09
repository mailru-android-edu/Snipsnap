package com.wndenis.snipsnap.calendar.components

import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import java.time.LocalDateTime

internal data class LocalDateTimeData(
    val localDateTime: LocalDateTime,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LocalDateTimeData
}
