package com.wndenis.snipsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.wndenis.snipsnap.ui.theme.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.datetime.datetimepicker
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    @ExperimentalComposeUiApi
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val name = intent.getStringExtra("name").toString()
        Log.i("got name",name)
        super.onCreate(savedInstanceState)
        setTheme(R.style.SplashScreenTheme)
        setContent {
            SnipsnapTheme {
                Scaffold{
                    Surface {
                        ScheduleCalendarDemo()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.i("back btn:","pressed");
        finish();
    }
}




    fun LocalDateTime.conv(): String {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    //getDateTimeInstance()//
    return this.format(df)
}

fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
}

@ExperimentalComposeUiApi
@Composable
fun EditEvents(event: CalendarEvent, dismissAction: () -> Unit) {
    val colors = listOf(P200, P75, R100, R500, T100, T200, T300, T500, Y100, Y300, Y400)
    val editedEvent by remember { mutableStateOf(event.copy()) }
    var unusedBool by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val updater = {
        unusedBool = !unusedBool
    }

    fun applyChanges() {
        event.mimic(editedEvent)
        dismissAction()
    }

    fun getSelectedColor(): Int {
        val i = colors.indexOf(editedEvent.color)
        if (i >= 0) return i
        return 0
    }


    val date1 = remember { MaterialDialog() }
    date1.build {
        datetimepicker(initialDateTime = editedEvent.startDate,
            is24HourClock = true,
            positiveButtonText = "OK",
            negativeButtonText = "Отмена",
//            datePickerTitle = "Выберите дату",
//            timePickerTitle = "Выберите время",
            onCancel = updater,
            onDateTimeChange = { dt ->
                editedEvent.startDate = dt
                updater()
            }
        )

    }

    val date2 = remember { MaterialDialog() }
    date2.build {
        datetimepicker(initialDateTime = editedEvent.endDate,
            is24HourClock = true,
            positiveButtonText = "OK",
            negativeButtonText = "Отмена",
            datePickerTitle = "Выберите дату",
            timePickerTitle = "Выберите время",
            onCancel = updater,
            onDateTimeChange = { dt ->
                editedEvent.endDate = dt
                updater()
            }
        )
    }

    val colorPicker = remember { MaterialDialog() }
    colorPicker.build {
        colorChooser(
            colors = colors,
            initialSelection = getSelectedColor()
        ) { color ->
            editedEvent.color = color
        }
        buttons {
            negativeButton("Отмена")
            positiveButton("OK", onClick = updater)
        }
//        colorPicker()
//        colorPicker(colors = ColorPalette.Primary)
    }

    Dialog(onDismissRequest = dismissAction) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            if (unusedBool) {
                Spacer(modifier = Modifier.width(0.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                //================= Header
                Row {
                    Column {
                        Text("Редактирование события")
                        Divider(thickness = 1.dp)
                    }
                }

                //================= Delete button
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.Red
                    ),
                    onClick = {
                        event.deleted = true
                        dismissAction()
                    }) {
                    Text("Удалить")
                }

                //================= Edit name
                Row {
                    Divider(thickness = 1.dp)
                }
                Row {
                    var oldName by remember { mutableStateOf(editedEvent.name) }
                    OutlinedTextField(
                        value = oldName,
                        onValueChange = {
                            var newStr = it
                            if (newStr.length > 25)
                                newStr = newStr.slice(0..25)
                            oldName = newStr
                            editedEvent.name = newStr
                        },
                        keyboardActions = KeyboardActions(
                            onAny = { hideKeyboard(context) }),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        label = { Text("Название события") })
                }

                //================= Color picker
                Row {
                    Box(
                        modifier = Modifier
//                            .fillMaxSize()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        Text(
                            "Выберите цвет",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { colorPicker.show() })
                                .background(
                                    editedEvent.color
                                )
                                .padding(10.dp)
                        )
                    }
                }
                Row {
                    Divider(thickness = 1.dp)
                }

                //================= Date picker 1
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Gray
                        ),
                        onClick = {
                            date1.show()
                        }) {
                        Text("Начало: " + editedEvent.startDate.conv(), color = Color.Black)
                    }
                }

                //================= Date picker 2
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Gray
                        ),
                        onClick = {
                            date2.show()
                        }) {
                        Text("Конец: " + editedEvent.endDate.conv(), color = Color.Black)
                    }
                }

                //================= Cancel/Save
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Yellow
                        ),
                        onClick = {
                            dismissAction()
                        }) {
                        Text("Отмена")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Green
                        ),
                        onClick = {
                            applyChanges()
                        }) {
                        Text("ОК")
                    }
                }
            }
        }
    }
}


@ExperimentalComposeUiApi
@Composable
fun ScheduleCalendarDemo() {
    val viewSpan = remember { mutableStateOf(48 * 3600L) }
    val eventTimesVisible = remember { mutableStateOf(true) }
    val eventSections = rememberSaveable { (0..25).map { CalendarSection() }.toMutableList() }
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
//        scale *= zoomChange
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
//
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "О, привет!\nОтправлено из моего крутого приложения)"
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
                    Log.i("event == null:", (editingEvent == null).toString())
                    EditEvents(event = editingEvent!!) {
                        editingEvent = null
                    }
                }
        }


//        EditEvents { doAddEvent = false }

        val calendarState = rememberScheduleCalendarState()

        Spacer(modifier = Modifier.height(8.dp))

        ScheduleCalendar(
            state = calendarState,
            now = LocalDateTime.now().plusHours(8),
            eventTimesVisible = eventTimesVisible.value,
            sections = eventSections,
            viewSpan = viewSpan.value,
            editor = editor,
            updater = updater
        )
    }
}
