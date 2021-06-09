package com.wndenis.snipsnap

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.wndenis.snipsnap.data.CalendarAdapter
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.data.CalendarSection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@ExperimentalAnimationApi
@Composable
fun ScheduleCalendar(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier,
    viewSpan: Long = 48 * 3600L, // in seconds
    now: LocalDateTime = LocalDateTime.now(),
    eventTimesVisible: Boolean = true,
    updater: () -> Unit,
    editor: (CalendarEvent) -> Unit,
    adapter: CalendarAdapter
) =
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            // .pointerInteropFilter {
            //     val singlePointer = it.pointerCount <= 1
            //     Log.i("Changes", "${it.pointerCount} $singlePointer")
            //     // state.canScroll.value = singlePointer
            //     !singlePointer
            // }
            // .pointerInput(Unit) {
            //     awaitPointerEventScope {
            //         val event = awaitPointerEvent(PointerEventPass.Main)
            //         Log.i("Changes",
            //             event.changes
            //                 .count()
            //                 .toString()
            //         )
            //         state.canScroll.value = event.changes.count() == 1
            //         // state.canScroll.value = this.currentEvent.changes.count() == 1
            //     }
            // }
            .scrollable(
                state.scrollableState,
                Orientation.Horizontal,
                flingBehavior = state.scrollFlingBehavior,
                // enabled = state.canScroll.value
            )

    ) {
        state.updateView(viewSpan, constraints.maxWidth)
        // DIVIDERS =============================================

        DayDividers(
            state = state,
            modifier = Modifier.matchParentSize()
        )
        MonthDividers(
            state = state,
            modifier = Modifier.matchParentSize()
        )
        YearDividers(
            state = state,
            modifier = Modifier.matchParentSize()
        )

        // CONTENT =============================================
        Column {
            // HEADERS =============================================
            YearRow(state = state)
            MonthRow(state = state)
            DaysRow(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            HoursRow(state)


            // EVENTS =============================================
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    adapter.sections.forEach {
                        CalendarSectionRow(
                            section = it,
                            state = state,
                            eventTimesVisible = eventTimesVisible,
                            updater = updater,
                            editor = editor
                        )
                        Divider()
                    }
                }

                // hour dividers
                Canvas(modifier = Modifier.matchParentSize()) {
                    state.visibleHours.forEach { localDateTime ->
                        val offsetPercent = state.offsetFraction(localDateTime)
                        drawLine(
                            color = Color.Gray,
                            strokeWidth = 2f,
                            start = Offset(offsetPercent * size.width, 0f),
                            end = Offset(offsetPercent * size.width, size.height),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(10f, 20f),
                                phase = 5f
                            )
                        )
                    }
                }
            }
        }
        val nowColor = MaterialTheme.colors.primary
        // "now" indicator =============================================
        Canvas(modifier = Modifier.matchParentSize()) {
            val offsetPercent = state.offsetFraction(now)
            drawLine(
                color = nowColor,
                strokeWidth = 6f,
                start = Offset(offsetPercent * size.width, 0f),
                end = Offset(offsetPercent * size.width, size.height)
            )
            drawCircle(
                nowColor,
                center = Offset(offsetPercent * size.width, 12f),
                radius = 12f
            )
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarSectionRow(
    section: CalendarSection,
    state: ScheduleCalendarState,
    eventTimesVisible: Boolean,
    updater: () -> Unit,
    editor: (CalendarEvent) -> Unit
) {
    section.gc()
    val interSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val width = this.size.width
                        val pressDateRatio = offset.x / width

                        val minStart = state.startDateTime.toEpochSecond(ZoneOffset.UTC)
                        val maxEnd = state.endDateTime.toEpochSecond(ZoneOffset.UTC)

                        val spanDuration = maxEnd - minStart
                        val eventHalfDuration = spanDuration / 6f

                        val pressMid = (maxEnd - minStart) * pressDateRatio
                        val newStart = (minStart + pressMid - eventHalfDuration).toLong()
                        val newEnd = (minStart + pressMid + eventHalfDuration).toLong()

                        val newStartTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(newStart),
                            ZoneId.systemDefault()
                        )
                        val newEndTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(newEnd),
                            ZoneId.systemDefault()
                        )

                        Log.i("Long tap", "${this.size.width} tap at ${offset}")


                        val newEvent = CalendarEvent(
                            startDate = newStartTime,
                            endDate = newEndTime,
                            name = "Новое событие",
                            color = ColorPalette.Primary.random()
                        )
                        section.events.add(newEvent)

                        // ripple effect
                        GlobalScope.launch {
                            val press = PressInteraction.Press(offset)
                            interSource.emit(press)
                            interSource.emit(PressInteraction.Release(press))
                        }

                        updater()
                    }
                )
            }
            .indication(
                interactionSource = interSource,
                indication = rememberRipple()
            )
    ) {
        val eventMap = section.events.map { event ->
            Triple(
                event,
                event.startDate.isAfter(state.startDateTime) &&
                        event.startDate.isBefore(state.endDateTime),
                event.endDate.isAfter(state.startDateTime) &&
                        event.endDate.isBefore(state.endDateTime),
            )
        }.filter { (event, startHit, endHit) ->
            startHit || endHit || (
                    event.startDate.isBefore(state.startDateTime) && event.endDate.isAfter(
                        state.endDateTime
                    )
                    )
        }

        if (eventMap.isNotEmpty()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                eventMap.forEach { (event, startHit, endHit) ->
                    val (width, offsetX) = state.widthAndOffsetForEvent(
                        start = event.startDate,
                        end = event.endDate,
                        totalWidth = constraints.maxWidth
                    )

                    val shape = when {
                        startHit && endHit -> RoundedCornerShape(4.dp)
                        startHit -> RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp
                        )
                        endHit -> RoundedCornerShape(
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                        else -> RoundedCornerShape(4.dp)
                    }

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(with(LocalDensity.current) { width.toDp() })
                            .offset { IntOffset(offsetX, 0) }
                            .background(event.color, shape = shape)
                            .clip(shape)
                            .clickable {
                                editor(event)
                                updater()
                            }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AnimatedVisibility(visible = eventTimesVisible) {
                            Text(
                                text = event.startDate
                                    .format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                        event.endDate
                                            .format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "",
                fontSize = 40.sp,
                fontWeight = W500,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


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


// DIVIDERS

val DASH_LIGHT = DashDecision(
    4f, PathEffect.dashPathEffect(
        intervals = floatArrayOf(10f, 20f),
        phase = 5f
    )
)
val DASH_MEDIUM = DashDecision(
    8f, PathEffect.dashPathEffect(
        intervals = floatArrayOf(20f, 10f),
        phase = 5f
    )
)
val DASH_BOLD = DashDecision(
    12f, PathEffect.dashPathEffect(
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

private data class LocalDateTimeData(
    val localDateTime: LocalDateTime,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LocalDateTimeData
}

private val Measurable.localDateTime: LocalDateTime
    get() = (parentData as? LocalDateTimeData)?.localDateTime
        ?: error("No LocalDateTime for measurable $this")

fun LocalDateTime.between(
    target: LocalDateTime,
    increment: LocalDateTime.() -> LocalDateTime
): Sequence<LocalDateTime> {
    return generateSequence(
        seed = this,
        nextFunction = {
            val next = it.increment()
            if (next.isBefore(target)) next else null
        }
    )
}

infix fun LocalDateTime.daysBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS)
    return start.between(target.truncatedTo(ChronoUnit.DAYS).plusDays(1)) {
        plusDays(1)
    }
}

infix fun LocalDateTime.monthsBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
    return start.between(
        target
            .truncatedTo(ChronoUnit.DAYS)
            .withDayOfMonth(1)
            .plusMonths(1)
    ) {
        plusMonths(1)
    }
}

infix fun LocalDateTime.yearsBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS).withMonth(1)
        .withDayOfMonth(1)
    return start.between(
        target
            .truncatedTo(ChronoUnit.DAYS)
            .withMonth(1)
            .withDayOfMonth(1)
            .plusYears(1)
    ) {
        plusYears(1)
    }
}
