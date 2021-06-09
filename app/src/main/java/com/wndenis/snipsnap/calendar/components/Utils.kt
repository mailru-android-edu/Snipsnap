package com.wndenis.snipsnap.calendar.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.Measurable
import com.wndenis.snipsnap.R
import com.wndenis.snipsnap.calendar.SpanType
import java.time.LocalDateTime

internal val Measurable.localDateTime: LocalDateTime
    get() = (parentData as? LocalDateTimeData)?.localDateTime
        ?: error("No LocalDateTime for measurable $this")

const val HOUR_SEC = 3600L
const val DAY_SEC = HOUR_SEC * 24
const val WEEK_SEC = DAY_SEC * 7
const val HALF_WEEK_SEC = WEEK_SEC / 2
const val TWO_WEEK_SEC = WEEK_SEC * 2
const val TWO_WEEK_A_HALF_SEC = WEEK_SEC * 2 + WEEK_SEC / 2
const val MONTH_SEC = DAY_SEC * 30 // wndenis: rough approx
const val THREE_MONTH_SEC = MONTH_SEC * 3
const val SIX_MONTH_SEC = MONTH_SEC * 6
const val YEAR_SEC = DAY_SEC * 365
const val TWO_YEAR_SEC = YEAR_SEC * 2
const val BIGGER_SEC = YEAR_SEC * 2 + 1


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
