package com.wndenis.snipsnap.calendar.calcomponents

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wndenis.snipsnap.calendar.ScheduleCalendarState
import com.wndenis.snipsnap.calendar.SpanType
import com.wndenis.snipsnap.utils.daysBetween
import com.wndenis.snipsnap.utils.monthsBetween
import com.wndenis.snipsnap.utils.yearsBetween
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun YearRow(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    val corScope = rememberCoroutineScope()
    val anim = remember { Animatable(calculateYearFontSize(state.spanType)) }
    corScope.launch { anim.animateTo(calculateYearFontSize(state.spanType)) }
    BoxWithConstraints(modifier = modifier) {
        (state.startDateTime yearsBetween state.endDateTime).forEach { localDateTime ->
            val (width, offsetX) = state.widthAndOffsetForEvent(
                start = localDateTime,
                end = localDateTime.plusYears(1),
                totalWidth = constraints.maxWidth
            )
            Column(
                modifier = Modifier
                    .width(with(LocalDensity.current) { width.toDp() })
                    .offset { IntOffset(offsetX, 0) }
                    .padding(horizontal = 8.dp)
            ) {
                val txt = localDateTime.format(DateTimeFormatter.ofPattern("yyyy"))
                Text(
                    text = txt,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = anim.value.sp,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@ExperimentalAnimationApi
@Composable
internal fun MonthRow(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    val spanType = state.spanType
    AnimatedVisibility(visible = spanType.ordinal <= SpanType.YEAR.ordinal) {
        BoxWithConstraints(modifier = modifier) {
            (state.startDateTime monthsBetween state.endDateTime).forEach { localDateTime ->
                val (width, offsetX) = state.widthAndOffsetForEvent(
                    start = localDateTime,
                    end = localDateTime.plusMonths(1),
                    totalWidth = constraints.maxWidth
                )
                Column(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { width.toDp() })
                        .offset { IntOffset(offsetX, 0) }
                        .padding(horizontal = 8.dp)
                ) {
                    var pattern = "LL"
                    if (spanType.ordinal <= SpanType.THREE_MONTH.ordinal)
                        pattern = "LLLL"
                    else if (spanType.ordinal <= SpanType.SIX_MONTH.ordinal)
                        pattern = "LLL"

                    Text(
                        text = localDateTime.format(DateTimeFormatter.ofPattern(pattern)),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun DaysRow(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    val corScope = rememberCoroutineScope()
    val anim = remember { Animatable(calculateDayFontSize(state.spanType)) }
    corScope.launch { anim.animateTo(calculateDayFontSize(state.spanType)) }
    AnimatedVisibility(visible = anim.value != 0f) {
        BoxWithConstraints(modifier = modifier) {
            (state.startDateTime daysBetween state.endDateTime).forEach { localDateTime ->
                val (width, offsetX) = state.widthAndOffsetForEvent(
                    start = localDateTime,
                    end = localDateTime.plusDays(1),
                    totalWidth = constraints.maxWidth
                )

                Column(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { width.toDp() })
                        .offset { IntOffset(offsetX, 0) }
                        .padding(horizontal = 8.dp)
                ) {
                    val txt = localDateTime.format(DateTimeFormatter.ofPattern("dd"))
                    Text(
                        text = txt,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        fontSize = anim.value.sp,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HoursRow(
    state: ScheduleCalendarState
) {
    AnimatedVisibility(visible = state.visibleHours.isNotEmpty()) {
        Layout(
            content = {
                state.visibleHours.forEach { localDateTime ->
                    Text(
                        localDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 12.sp,
                        modifier = Modifier.then(
                            LocalDateTimeData(localDateTime)
                        )
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) to it.localDateTime }

            val width = constraints.maxWidth
            val height = if (placeables.isNotEmpty()) {
                placeables.maxOf { it.first.height }
            } else {
                0
            }
            layout(width, height) {
                placeables.forEach { (placeable, localDateTime) ->
                    val origin = state.offsetFraction(localDateTime) * width
                    val x = origin.toInt() - placeable.width / 2
                    placeable.placeRelative(x.coerceAtLeast(0), 0)
                }
            }
        }
    }
}
