package com.wndenis.snipsnap.calendar.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.wndenis.snipsnap.calendar.ScheduleCalendarState
import com.wndenis.snipsnap.calendar.SpanType
import com.wndenis.snipsnap.utils.daysBetween
import com.wndenis.snipsnap.utils.monthsBetween
import com.wndenis.snipsnap.utils.yearsBetween

// DIVIDERS

@ExperimentalAnimationApi
@Composable
fun YearDividers(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        (state.startDateTime yearsBetween state.endDateTime).forEach { localDateTime ->
            val offsetPercent = state.offsetFraction(localDateTime)
            val dash = decideDash(
                state.spanType,
                SpanType.YEAR to DASH_BOLD,
                SpanType.BIGGER to DASH_MEDIUM
            )
            drawLine(
                color = Color.Gray,
                strokeWidth = dash.first,
                start = Offset(offsetPercent * size.width, 0f),
                end = Offset(offsetPercent * size.width, size.height),
                pathEffect = dash.second
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun MonthDividers(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    val spanType = state.spanType
    if (spanType.ordinal <= SpanType.TWO_YEAR.ordinal) {
        Canvas(modifier = modifier) {
            (state.startDateTime monthsBetween state.endDateTime).forEach { localDateTime ->
                val offsetPercent = state.offsetFraction(localDateTime)
                val dash = decideDash(
                    state.spanType,
                    SpanType.THREE_MONTH to DASH_MEDIUM,
                    SpanType.BIGGER to DASH_LIGHT
                )
                drawLine(
                    color = Color.Gray,
                    strokeWidth = dash.first,
                    start = Offset(offsetPercent * size.width, 0f),
                    end = Offset(offsetPercent * size.width, size.height),
                    pathEffect = dash.second
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun DayDividers(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    val spanType = state.spanType
    if (spanType.ordinal < SpanType.THREE_MONTH.ordinal) {
        Canvas(modifier = modifier) {
            (state.startDateTime daysBetween state.endDateTime).forEach { localDateTime ->
                val offsetPercent = state.offsetFraction(localDateTime)
                drawLine(
                    color = Color.Gray,
                    strokeWidth = DASH_LIGHT.first,
                    start = Offset(offsetPercent * size.width, 0f),
                    end = Offset(offsetPercent * size.width, size.height),
                    pathEffect = DASH_LIGHT.second
                )
            }
        }
    }
}
