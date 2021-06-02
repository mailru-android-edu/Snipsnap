package com.wndenis.snipsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wndenis.snipsnap.data.CalendarAdapter
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.ui.theme.*
import java.time.LocalDateTime

class ContextKeeper {
    companion object {
        private lateinit var context: Context
        fun setContext(c: Context) {
            context = c
        }
    }
}

class MainActivity : ComponentActivity() {
    companion object{
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
        MainActivity.activity = this
        setTheme(R.style.SplashScreenTheme)

        val calAdapter = CalendarAdapterCreator(isNew, name)
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
        finishAndRemoveTask();
    }
}

fun CalendarAdapterCreator(isNew: Boolean, name: String): CalendarAdapter? {
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
                }) {
                Text("Очень жаль")
            }
        },
    )
}

@ExperimentalComposeUiApi
@Composable
fun ScheduleCalendarDemo(passedCalendarAdapter: CalendarAdapter, howToExit: () -> Unit) {
    val viewSpan = remember { mutableStateOf(48 * 3600L) }
    val eventTimesVisible = remember { mutableStateOf(true) }
    val calendarAdapter by rememberSaveable(stateSaver = CalendarAdapter.AdapterSaver) {
        mutableStateOf(
            passedCalendarAdapter
        )
    }
    calendarAdapter.exportToFile()

    //val eventSections = rememberSaveable { (0..25).map { CalendarSection() }.toMutableList() }
    var doAddEvent by remember { mutableStateOf(false) }
    var editingEvent: CalendarEvent? by remember { mutableStateOf(null) }

    val editor = { event: CalendarEvent ->
        editingEvent = event
    }

    val updater = {
        doAddEvent = true
    }

    val context = LocalContext.current


    var scale by remember { mutableStateOf(1f) }
    val state = rememberTransformableState { zoomChange, _, _ ->
        scale *= zoomChange
    }


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
            )
            .transformable(state = state)
    ) {
        Row {
            IconButton(onClick = {
                calendarAdapter.exportToFile()
                howToExit()
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
            }

            IconButton(onClick = {
                viewSpan.value = (viewSpan.value * 2).coerceAtMost(96 * 3600)
            }) {
                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "increase")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                viewSpan.value = (viewSpan.value / 2).coerceAtLeast(3 * 3600)
            }) {
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "decrease")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                eventTimesVisible.value = !(eventTimesVisible.value)
            }) {
                Icon(imageVector = Icons.Default.HideImage, contentDescription = "decrease")
            }

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
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
            }) {
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

        val calendarState = rememberScheduleCalendarState()

        Spacer(modifier = Modifier.height(8.dp))

        ScheduleCalendar(
            state = calendarState,
            now = LocalDateTime.now(),
            eventTimesVisible = eventTimesVisible.value,
            adapter = calendarAdapter,
            viewSpan = viewSpan.value,
            editor = editor,
            updater = updater
        )
    }
}
