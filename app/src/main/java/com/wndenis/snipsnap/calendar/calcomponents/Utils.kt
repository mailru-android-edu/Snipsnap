package com.wndenis.snipsnap.calendar.calcomponents

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import com.wndenis.snipsnap.calendar.SpanType
import java.time.LocalDateTime

internal data class LocalDateTimeData(
    val localDateTime: LocalDateTime,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LocalDateTimeData
}

internal val Measurable.localDateTime: LocalDateTime
    get() = (parentData as? LocalDateTimeData)?.localDateTime
        ?: error("No LocalDateTime for measurable $this")

const val YEAR_SIZE_ZERO = 0f
const val YEAR_SIZE_MICRO = 6f
const val YEAR_SIZE_SMALL = 12f
const val YEAR_SIZE_REGULAR = 18f

fun calculateYearFontSize(spanType: SpanType): Float {
    return when (spanType) {
        SpanType.LESSER, SpanType.WEEK, SpanType.TWO_WEEK -> YEAR_SIZE_ZERO
        SpanType.TWO_WEEK_A_HALF, SpanType.MONTH -> YEAR_SIZE_MICRO
        SpanType.THREE_MONTH, SpanType.SIX_MONTH -> YEAR_SIZE_SMALL
        SpanType.YEAR, SpanType.TWO_YEAR, SpanType.BIGGER -> YEAR_SIZE_REGULAR
    }
}

const val DAY_SIZE_ZERO = 0f
const val DAY_SIZE_MICRO = 6f
const val DAY_SIZE_SMALL = 12f
const val DAY_SIZE_REGULAR = 18f

fun calculateDayFontSize(spanType: SpanType): Float {
    return when (spanType) {
        SpanType.LESSER, SpanType.WEEK -> DAY_SIZE_REGULAR
        SpanType.TWO_WEEK -> DAY_SIZE_SMALL
        SpanType.TWO_WEEK_A_HALF -> DAY_SIZE_MICRO
        else -> DAY_SIZE_ZERO
    }
}

// DIVIDERS

val DASH_LIGHT = DashDecision(
    4f,
    PathEffect.dashPathEffect(
        intervals = floatArrayOf(10f, 20f),
        phase = 5f
    )
)
val DASH_MEDIUM = DashDecision(
    8f,
    PathEffect.dashPathEffect(
        intervals = floatArrayOf(20f, 10f),
        phase = 5f
    )
)
val DASH_BOLD = DashDecision(
    12f,
    PathEffect.dashPathEffect(
        intervals = floatArrayOf(40f, 5f),
        phase = 5f
    )
)

typealias DashDecision = Pair<Float, PathEffect>

fun decideDash(spanType: SpanType, vararg pairs: Pair<SpanType, DashDecision>): DashDecision {
    for (elem in pairs) {
        if (spanType.ordinal <= elem.first.ordinal)
            return elem.second
    }
    return DASH_LIGHT
}
