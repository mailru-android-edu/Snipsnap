package com.wndenis.snipsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wndenis.snipsnap.calendar.ScheduleCalendar
import com.wndenis.snipsnap.calendar.components.DAY_SEC
import com.wndenis.snipsnap.calendar.components.HOUR_SEC
import com.wndenis.snipsnap.calendar.components.YEAR_SEC
import com.wndenis.snipsnap.calendar.rememberScheduleCalendarState
import com.wndenis.snipsnap.data.CalendarAdapter
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.ui.theme.SnipsnapTheme

const val MAX_SPAN = 3 * YEAR_SEC
const val MIN_SPAN = 12 * HOUR_SEC
const val DAYS_IN_THE_WEEK = 7
val WIDTH_8 = 8.dp

val HEIGHT_8 = 8.dp
val PADDING_8 = 8.dp

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var activity: ComponentActivity
        fun getContext(): Context {
            return activity.applicationContext
        }
    }

    var saveLastResort = {}

    @ExperimentalComposeUiApi
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val name = intent.getStringExtra("name").toString()
        val isNew = intent.getBooleanExtra("isNew", true)

        super.onCreate(savedInstanceState)
        activity = this

        val calAdapter = calendarAdapterCreator(isNew, name)
        calAdapter?.let { saveLastResort = calAdapter::exportToFile }

        setContent {
            SnipsnapTheme {
                Scaffold {
                    if (calAdapter != null) {
                        Surface {
                            ScheduleCalendarDemo(calAdapter) { onBackPressed() }
                        }
                    } else {
                        Surface {
                            ErrorDialog { onBackPressed() }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        saveLastResort()
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}

fun calendarAdapterCreator(isNew: Boolean, name: String): CalendarAdapter? {
    if (isNew)
        return CalendarAdapter(name)

    return CalendarAdapter.importFromFile(name)
}

@Composable
fun ErrorDialog(howToExit: () -> Unit) {
    AlertDialog(
        title = { Text("Не удалось загрузить файл") },
        onDismissRequest = howToExit,
        confirmButton = {
            Button(
                onClick = {
                    howToExit()
                }
            ) {
                Text("Очень жаль")
            }
        },
    )
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ScheduleCalendarDemo(passedCalendarAdapter: CalendarAdapter, howToExit: () -> Unit) {
    val defaultViewSpan = remember { DAYS_IN_THE_WEEK * DAY_SEC }
    val viewSpan = remember { mutableStateOf(defaultViewSpan) }
    val calendarAdapter by rememberSaveable(stateSaver = CalendarAdapter.AdapterSaver) {
        mutableStateOf(
            passedCalendarAdapter
        )
    }
    calendarAdapter.exportToFile()

    var doAddEvent by remember { mutableStateOf(false) }
    var editingEvent: CalendarEvent? by remember { mutableStateOf(null) }

    val editor = { event: CalendarEvent ->
        editingEvent = event
    }

    val updater = {
        doAddEvent = true
    }

    val context = LocalContext.current

    // var scale by remember { mutableStateOf(1f) }
    // val state = rememberTransformableState { zoomChange, _, _ ->
    //     viewSpan.value =
    //         (viewSpan.value.toFloat() / zoomChange).toLong()
    //             .coerceAtMost(MAX_SPAN)
    //             .coerceAtLeast(MIN_SPAN)
    // }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    val event = awaitPointerEvent()
                    Log.i("Changes", "${event.changes.count()}")
                }
                detectTransformGestures { _, _, zoom, _ ->
                    viewSpan.value =
                        (viewSpan.value.toFloat() / zoom)
                            .toLong()
                            .coerceAtMost(MAX_SPAN)
                            .coerceAtLeast(MIN_SPAN)
                }
            }
        // .transformable(state = state)
        // .pointerInput(Unit) {
        //     detectTransformGestures {_, _, zoomChange, _ ->
        //         (viewSpan.value.toFloat() / zoomChange).toLong()
        //             .coerceAtMost(MAX_SPAN)
        //             .coerceAtLeast(MIN_SPAN)
        //
        //     }
        // }
    ) {
        val calendarState = rememberScheduleCalendarState(sectionEditor = editor)

        Row {
            IconButton(
                onClick = {
                    calendarAdapter.exportToFile()
                    howToExit()
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
            }

            IconButton(
                onClick = {
                    viewSpan.value = (viewSpan.value * 2).coerceAtMost(MAX_SPAN)
                }
            ) {
                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "increase")
            }
            Spacer(modifier = Modifier.width(WIDTH_8))
            IconButton(
                onClick = {
                    viewSpan.value = defaultViewSpan.coerceAtLeast(defaultViewSpan)
                    calendarState.scrollToNow(defaultViewSpan)
                }
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
            }
            Spacer(modifier = Modifier.width(WIDTH_8))
            IconButton(
                onClick = {
                    viewSpan.value = (viewSpan.value / 2).coerceAtLeast(MIN_SPAN)
                }
            ) {
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "decrease")
            }
            // Spacer(modifier = Modifier.width(8.dp))
            // IconButton(
            //     onClick = {
            //         eventTimesVisible.value = !(eventTimesVisible.value)
            //     }
            // ) {
            //     Icon(imageVector = Icons.Default.Description, contentDescription = "description")
            // }

            Spacer(modifier = Modifier.width(WIDTH_8))
            IconButton(
                onClick = {
                    val text = calendarAdapter.exportToString()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            text
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "share")
            }
        }

        if (doAddEvent) {
            doAddEvent = false
        }

        editingEvent?.let {
            if (editingEvent != null)
                if (!editingEvent!!.deleted) {
                    EditEvents(event = editingEvent!!) {
                        editingEvent = null
                    }
                }
        }

        Spacer(modifier = Modifier.height(WIDTH_8))

        ScheduleCalendar(
            state = calendarState,
            adapter = calendarAdapter,
            viewSpan = viewSpan.value,
            updater = updater
        )
    }
}
