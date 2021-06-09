package com.wndenis.snipsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.wndenis.snipsnap.ui.theme.SnipsnapTheme
import com.wndenis.snipsnap.utils.conv
import com.wndenis.snipsnap.utils.extractName
import com.wndenis.snipsnap.utils.hideKeyboard
import com.wndenis.snipsnap.utils.makeName
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class DiagramFile(
    var fileName: String = "",
    var date: LocalDateTime = LocalDateTime.now(),
    var folderPath: String = "",
    var fullPath: String = ""
)

var FABClick: (() -> Unit)? = null

class MenuActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @OptIn(ExperimentalAnimationApi::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SnipsnapTheme {

                Scaffold(
                    topBar = { TopBarMain() },
                    floatingActionButton = { AddButton(this) }
                ) {
                    val diagrams = remember { mutableStateListOf<DiagramFile>() }
                    val updater = {
                        diagrams.clear()
                        updateFileList(diagrams)
                    }
                    updater()
                    DiagramList(diagrams, updater, this)
                }
            }
        }
    }

    fun updateFileList(diagrams: MutableList<DiagramFile>) {
        val files: Array<String> = this.fileList()
        val path = this.filesDir
        files.map {
            File(path, it)
        }.filter { it.exists() }
            .filter { it.name.endsWith("spsp") }
            .sortedByDescending { it.lastModified() }.map {
                diagrams.add(
                    DiagramFile(
                        it.name,
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(it.lastModified()),
                            ZoneId.systemDefault()
                        ),
                        it.parent!!,
                        it.absolutePath
                    )
                )
            }
    }
}

fun startEditing(name: String, isNew: Boolean, context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("name", name)
    intent.putExtra("isNew", isNew)
    context.startActivity(intent)
}

@Composable
fun TopBarMain() {
    TopAppBar(
        title = { Text("Мои диаграммы") },
        navigationIcon = {
        },
        actions = {}
    )
}

@ExperimentalMaterialApi
@Composable
fun DiagramCard(
    diagram: DiagramFile,
    onClick: () -> Unit,
    updater: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp, 5.dp, 5.dp, 5.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .align(Alignment.CenterVertically)
                    .weight(5f)
            ) {
                Text(
                    text = diagram.fileName.substring(0, diagram.fileName.length - 5),
                    style = TextStyle(fontSize = (18.sp)),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = diagram.date.conv(),
                        style = typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 25.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(5f)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End
            ) {
                Row {
                    // IconButton(
                    //     onClick = {
                    //
                    //         val sendIntent = Intent().apply {
                    //             action = Intent.ACTION_SEND
                    //             putExtra(
                    //                 Intent.EXTRA_STREAM,
                    //                 Uri.parse("file://" + diagram.fullPath)
                    //             )
                    //             putExtra(Intent.EXTRA_SUBJECT, "Поделиться диаграммой")
                    //             putExtra(Intent.EXTRA_TEXT, "Ура, прилетела диаграмма")
                    //             type = "application/json"
                    //         }
                    //         val shareIntent =
                    //             Intent.createChooser(sendIntent, "Поделиться диаграммой")
                    //         context.startActivity(shareIntent)
                    //     }
                    // ) {
                    //     Icon(
                    //         imageVector = Icons.Filled.Share,
                    //         contentDescription = "export"
                    //     )
                    // }
                    IconButton(
                        onClick = {
                            onEdit()
                        }
                    ) { Icon(imageVector = Icons.Filled.Edit, contentDescription = "change") }
                    IconButton(
                        onClick = {
                            val newName = makeName("${extractName(diagram.fileName)}_copy")
                            val destFile = File(diagram.folderPath, newName)
                            File(diagram.fullPath).copyTo(destFile, overwrite = true)
                            updater()
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "copy")
                    }

                    IconButton(
                        onClick = {
                            File(diagram.fullPath).delete()
                            updater()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun initSelected(): DiagramFile? {
    return null
}

@ExperimentalMaterialApi
@Composable
fun DiagramList(diagrams: MutableList<DiagramFile>, updater: () -> Unit, context: Context) {
    val nameChanger by remember { mutableStateOf(MaterialDialog()) }
    var selectedDiagram by remember { mutableStateOf(initSelected()) }
    var nameChangerAction = remember { { _: String -> } }

    val renameAndCreate = { newName: String ->
        startEditing(newName, true, context)
    }

    val renameExisting = { newName: String ->
        val filename = makeName(newName)
        File(selectedDiagram!!.fullPath).renameTo(File(selectedDiagram!!.folderPath, filename))
        updater()
    }

    FABClick = {
        selectedDiagram = DiagramFile()
        nameChangerAction = renameAndCreate
    }

    nameChanger.build {
        var oldName by remember { mutableStateOf("" + extractName(selectedDiagram?.fileName)) }
        val textFieldValueState = remember {
            mutableStateOf(
                TextFieldValue(
                    text = oldName,
                    selection = TextRange(oldName.length)
                )
            )
        }
        Row(
            Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textFieldValueState.value,
                onValueChange = { tfv ->
                    var newStr = tfv.text
                    if (newStr.length > 25)
                        newStr = newStr.slice(0..25)
                    oldName = newStr
                    textFieldValueState.value = tfv.copy(text = oldName)
                },
                isError = oldName.isEmpty(),
                keyboardActions = KeyboardActions(
                    onAny = { hideKeyboard(context) }
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                label = { Text("Название диаграммы") }
            )
        }

        buttons {
            negativeButton(
                "Отмена",
                onClick = { selectedDiagram = null }
            )
            if (textFieldValueState.value.text.isNotEmpty()) {
                positiveButton(
                    "OK",
                    onClick = {
                        nameChangerAction(oldName)
                        selectedDiagram = null
                    }
                )
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(items = diagrams) { _, d ->
            DiagramCard(
                diagram = d,
                onClick = { startEditing(d.fileName, false, context) },
                onEdit = {
                    nameChangerAction = renameExisting
                    selectedDiagram = d
                },
                updater = updater
            )
        }

        selectedDiagram?.let {
            Log.i("OMG", "IT WORKS")
            nameChanger.show()
        }
    }
}

@Composable
fun AddButton(context: Context) {
    FloatingActionButton(
        backgroundColor = MaterialTheme.colors.primary,
        onClick = { FABClick?.let { FABClick!!() } }
    ) {
        Icon(Icons.Filled.Add, contentDescription = "add a diagram")
    }
}
