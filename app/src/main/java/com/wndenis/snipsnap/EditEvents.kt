package com.wndenis.snipsnap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.datetime.datetimepicker
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.ui.theme.DpConst
import com.wndenis.snipsnap.utils.conv
import com.wndenis.snipsnap.utils.hideKeyboard
import java.time.LocalDateTime

const val STR_LENGTH = 25

@Composable
internal fun GetDatePicker(
    materialDialog: MaterialDialog,
    initialDateTime: LocalDateTime,
    updater: () -> Unit,
    onDateTimeChange: (LocalDateTime) -> Unit
) {
    materialDialog.build {
        datetimepicker(
            initialDateTime = initialDateTime,
            is24HourClock = true,
            positiveButtonText = "OK",
            negativeButtonText = "Отмена",
            datePickerTitle = "Дата",
            timePickerTitle = "Время",
            onCancel = updater,
            onDateTimeChange = { dt ->
                onDateTimeChange(dt)
                updater()
            }
        )
    }
}

@Composable
internal fun GetColorPicker(
    materialDialog: MaterialDialog,
    initialSelection: Int = 0,
    updater: () -> Unit,
    onChangeColor: (Color) -> Unit
) {
    materialDialog.build {
        colorChooser(
            colors = ColorPalette.Primary,
            initialSelection = initialSelection,
            onColorSelected = onChangeColor
        )
        buttons {
            negativeButton("Отмена")
            positiveButton("OK", onClick = updater)
        }
    }
}

fun getSelectedColor(color: Color): Int {
    val i = ColorPalette.Primary.indexOf(color)
    if (i >= 0) return i
    return 0
}

@ExperimentalComposeUiApi
@Composable
fun EditEvents(event: CalendarEvent, dismissAction: () -> Unit) {
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

    val date1 = remember { MaterialDialog() }
    GetDatePicker(date1, editedEvent.startDate, updater, { dt -> editedEvent.startDate = dt })

    val date2 = remember { MaterialDialog() }
    GetDatePicker(date2, editedEvent.endDate, updater, { dt -> editedEvent.endDate = dt })

    val colorPicker = remember { MaterialDialog() }
    GetColorPicker(
        colorPicker,
        getSelectedColor(editedEvent.color),
        updater,
        { color -> editedEvent.color = color }
    )

    Dialog(onDismissRequest = dismissAction) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(DpConst.DST_24),
            shape = RoundedCornerShape(DpConst.DST_32),
            color = MaterialTheme.colors.surface
        ) {
            if (unusedBool) {
                Spacer(modifier = Modifier.width(0.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DpConst.DST_16),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // ================= Header
                Row {
                    Column {
                        Text("Редактирование события")
                        Divider(thickness = 1.dp)
                    }
                }

                // ================= Delete button
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.error,
                        contentColor = MaterialTheme.colors.onError
                    ),
                    onClick = {
                        event.deleted = true
                        dismissAction()
                    }
                ) {
                    Text("Удалить", style = MaterialTheme.typography.button)
                }

                // ================= Edit name
                Row { Divider(thickness = 1.dp) }
                Row {
                    var oldName by remember { mutableStateOf(editedEvent.name) }
                    OutlinedTextField(
                        value = oldName,
                        onValueChange = {
                            var newStr = it
                            if (newStr.length > STR_LENGTH)
                                newStr = newStr.slice(0..STR_LENGTH)
                            oldName = newStr
                            editedEvent.name = newStr
                        },
                        keyboardActions = KeyboardActions(onAny = { hideKeyboard(context) }),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                        label = { Text("Название события") }
                    )
                }

                // ================= Color picker
                Row {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart)
                            .clip(MaterialTheme.shapes.small)
                    ) {
                        Text(
                            "Выберите цвет",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { colorPicker.show() })
                                .background(editedEvent.color)
                                .padding(DpConst.DST_10)
                        )
                    }
                }
                Row { Divider(thickness = 1.dp) }

                // ================= Date picker 1
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(backgroundColor = Color.Gray),
                        onClick = { date1.show() }
                    ) {
                        Text("Начало: " + editedEvent.startDate.conv(), color = Color.Black)
                    }
                }

                // ================= Date picker 2
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(backgroundColor = Color.Gray),
                        onClick = { date2.show() }
                    ) { Text("Конец: " + editedEvent.endDate.conv(), color = Color.Black) }
                }

                // ================= Cancel/Save
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { dismissAction() }
                    ) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(DpConst.DST_10))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { applyChanges() }
                    ) { Text("ОК") }
                }
            }
        }
    }
}
