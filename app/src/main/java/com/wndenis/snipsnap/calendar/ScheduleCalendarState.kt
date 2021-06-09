package com.wndenis.snipsnap.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wndenis.snipsnap.calendar.components.DAY_SEC
import com.wndenis.snipsnap.calendar.components.HALF_WEEK_SEC
import com.wndenis.snipsnap.calendar.components.HOUR_SEC
import com.wndenis.snipsnap.calendar.components.MONTH_SEC
import com.wndenis.snipsnap.calendar.components.SIX_MONTH_SEC
import com.wndenis.snipsnap.calendar.components.THREE_MONTH_SEC
import com.wndenis.snipsnap.calendar.components.TWO_WEEK_A_HALF_SEC
import com.wndenis.snipsnap.calendar.components.TWO_WEEK_SEC
import com.wndenis.snipsnap.calendar.components.TWO_YEAR_SEC
import com.wndenis.snipsnap.calendar.components.WEEK_SEC
import com.wndenis.snipsnap.calendar.components.YEAR_SEC
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.utils.between
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun rememberScheduleCalendarState(
    referenceDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS),
    sectionEditor: (CalendarEvent) -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit = {}
): ScheduleCalendarState {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) {
        ScheduleCalendarState(
            referenceDateTime = referenceDateTime,
            sectionEditor = sectionEditor,
            onDateTimeSelected = onDateTimeSelected,
            coroutineScope = coroutineScope,
        )
    }
}

class ScheduleCalendarState(
    referenceDateTime: LocalDateTime,
    val sectionEditor: (CalendarEvent) -> Unit,
    private val onDateTimeSelected: (LocalDateTime) -> Unit,
    private val coroutineScope: CoroutineScope
) {
    val startDateTime: LocalDateTime by derivedStateOf {
        referenceDateTime.plusSeconds(secondsOffset.value)
    }

    val endDateTime: LocalDateTime by derivedStateOf {
        startDateTime.plusSeconds(this.viewSpanSeconds.value)
    }

    val spanType: SpanType by derivedStateOf {
        spanToType(viewSpanSeconds.value)
    }

    fun viewSpanSeconds(): Long {
        return viewSpanSeconds.value
    }

    private val viewSpanSeconds = Animatable(ChronoUnit.DAYS.duration.seconds, LongToVector)
    private val secondsOffset = Animatable(0L, LongToVector)
    private val width = mutableStateOf(1)

    private var canUpdateView = true
    var canScroll = mutableStateOf(true)

    internal fun updateView(newViewSpanSeconds: Long, newWidth: Int? = null) {
        if (!canUpdateView)
            return
        newWidth?.let { this.width.value = newWidth }

        val currentViewSpanSeconds = viewSpanSeconds.value
        coroutineScope.launch {
            viewSpanSeconds.animateTo(newViewSpanSeconds)
        }
        coroutineScope.launch {
            if (newViewSpanSeconds != currentViewSpanSeconds) {
                updateAnchors(newViewSpanSeconds)
            }
        }
    }

    internal val scrollableState = ScrollableState {
        coroutineScope.launch {
            secondsOffset.snapTo(secondsOffset.value - it.toSeconds())
        }
        it
    }

    val SPAN_PROPROTION = -1/4

    fun scrollToNow(newSpan: Long) {
        coroutineScope.launch {
            canUpdateView = false
            secondsOffset.animateTo(newSpan * SPAN_PROPROTION)
        }
        coroutineScope.launch {
            canUpdateView = false
            viewSpanSeconds.animateTo(newSpan)
            canUpdateView = true
            // wndenis: sorry for this spaghetti, I had no time to deal with it
            updateView(newSpan)
        }
    }

    private val secondsInPx by derivedStateOf {
        this.viewSpanSeconds.value.toFloat() / width.value.toFloat()
    }

    private fun Float.toSeconds(): Long {
        return (this * secondsInPx).roundToLong()
    }

    private fun Long.toPx(): Float {
        return this / secondsInPx
    }

    internal val scrollFlingBehavior = object : FlingBehavior {
        val decay = exponentialDecay<Float>(frictionMultiplier = 2f)
        override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
            val endPosition = decay.calculateTargetValue(0f, -initialVelocity)
            flingToNearestAnchor(secondsOffset.value.toPx() + endPosition)
            return 0f
        }
    }

    internal val visibleHours by derivedStateOf {
        val startHour = startDateTime.truncatedTo(ChronoUnit.HOURS)
        val endHour = endDateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1)

        if (anchorRangeSeconds == H24) {
            emptyList()
        } else {
            startHour.between(endHour) { plusHours(1L) }.filter {
                it.hour % (anchorRangeSeconds / HOUR_SEC) == 0L && it.hour != 0
            }.toList()
        }
    }

    private var anchorRangeSeconds by mutableStateOf(Long.MAX_VALUE)
    private var anchorRangePx by mutableStateOf(Float.MAX_VALUE)

    private val H24 = HOUR_SEC * 24
    private val H12 = HOUR_SEC * 12
    private val H6 = HOUR_SEC * 6
    private val H3 = HOUR_SEC * 3
    private val H2 = HOUR_SEC * 2
    private val H1 = HOUR_SEC * 1

    private suspend fun updateAnchors(viewSpanInSeconds: Long) {
        anchorRangeSeconds = if (viewSpanInSeconds > H24) {
            H24
        } else if (viewSpanInSeconds <= H24 && viewSpanInSeconds > H12) {
            H6
        } else if (viewSpanInSeconds <= H12 && viewSpanInSeconds > H6) {
            H3
        } else if (viewSpanInSeconds <= H6 && viewSpanInSeconds > H3) {
            H2
        } else {
            H1
        }
        anchorRangePx = anchorRangeSeconds.toPx()
        flingToNearestAnchor(secondsOffset.value.toPx())
    }

    private suspend fun flingToNearestAnchor(target: Float) {
        val nearestAnchor = target - (target.absRem(anchorRangePx))
        val nearestAnchor2 = nearestAnchor + anchorRangePx

        val newAnchoredPosition =
            (abs(target - nearestAnchor) to abs(target - nearestAnchor2)).let {
                if (it.first > it.second) nearestAnchor2 else nearestAnchor
            }
        secondsOffset.animateTo(newAnchoredPosition.toSeconds())
        onDateTimeSelected(startDateTime)
    }

    internal fun offsetFraction(localDateTime: LocalDateTime): Float {
        return ChronoUnit.SECONDS.between(startDateTime, localDateTime)
            .toFloat() / (viewSpanSeconds.value).toFloat()
    }

    internal fun widthAndOffsetForEvent(
        start: LocalDateTime,
        end: LocalDateTime,
        totalWidth: Int
    ): Pair<Int, Int> {
        val startOffsetPercent = offsetFraction(start).coerceIn(0f, 1f)
        val endOffsetPercent = offsetFraction(end).coerceIn(0f, 1f)

        val width = ((endOffsetPercent - startOffsetPercent) * totalWidth).toInt() + 1
        val offsetX = (startOffsetPercent * totalWidth).toInt()
        return width to offsetX
    }
}

enum class SpanType {
    LESSER, WEEK, TWO_WEEK, TWO_WEEK_A_HALF, MONTH, THREE_MONTH, SIX_MONTH, YEAR, TWO_YEAR, BIGGER
}

private fun spanToType(span: Long): SpanType {
    return when {
        span <= HALF_WEEK_SEC -> SpanType.LESSER
        span <= WEEK_SEC -> SpanType.WEEK
        span <= TWO_WEEK_SEC -> SpanType.TWO_WEEK
        span <= TWO_WEEK_A_HALF_SEC -> SpanType.TWO_WEEK_A_HALF
        span <= MONTH_SEC -> SpanType.MONTH
        span <= THREE_MONTH_SEC -> SpanType.THREE_MONTH
        span <= SIX_MONTH_SEC -> SpanType.SIX_MONTH
        span <= YEAR_SEC -> SpanType.YEAR
        span <= TWO_YEAR_SEC -> SpanType.TWO_YEAR
        else -> SpanType.BIGGER
    }
}

private val LongToVector: TwoWayConverter<Long, AnimationVector1D> =
    TwoWayConverter({ AnimationVector1D(it.toFloat()) }, { it.value.toLong() })

private fun Float.absRem(modular: Float): Float {
    return ((this % modular) + modular) % modular
}
