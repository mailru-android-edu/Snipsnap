package com.wndenis.snipsnap
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.wndenis.snipsnap.ui.theme.Y400
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

class MenuActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @OptIn(ExperimentalAnimationApi::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
        Log.i("FILEs: ", diagrams.toString())
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
    updater: () -> Unit
) {
    val context = LocalContext.current
    val nameChanger by remember { mutableStateOf(MaterialDialog()) }
    nameChanger.build {
        var oldName by remember { mutableStateOf("" + diagram.fileName) }
        Row {

            OutlinedTextField(
                value = oldName,
                onValueChange = {
                    var newStr = it
                    if (newStr.length > 25)
                        newStr = newStr.slice(0..25)
                    oldName = newStr
                    diagram.fileName = newStr
                },
                keyboardActions = KeyboardActions(
                    onAny = { hideKeyboard(context) }
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                label = { Text("Название диаграммы") }
            )
        }
        buttons {
            negativeButton("Отмена")
            positiveButton(
                "OK",
                onClick = {
                    File(diagram.fullPath).renameTo(File(diagram.folderPath, oldName))
                    updater()
                }
            )
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 5.dp,
                top = 5.dp,
                start = 5.dp,
                end = 5.dp
            )
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp,

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
                    style = TextStyle(
                        fontSize = (18.sp)
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = diagram.date.conv(),
                        style = typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            end = 25.dp
                        )
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
                    IconButton(
                        onClick = {

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    Uri.parse("file://" + diagram.fullPath)
                                )
                                putExtra(
                                    Intent.EXTRA_SUBJECT, "Поделиться диаграммой"
                                )
                                putExtra(Intent.EXTRA_TEXT, "Ура, прилетела диаграмма")

                                type = "application/json"
                            }
                            val shareIntent =
                                Intent.createChooser(sendIntent, "Поделиться диаграммой")
                            // context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = "export", tint = Color.White )
                    }
                    IconButton(
                        onClick = {
                            nameChanger.show()
                        }
                    ) { Icon(imageVector = Icons.Filled.Edit, contentDescription = "change") }
                    IconButton(
                        onClick = {
                            val newName =
                                "${
                                diagram.fileName.subSequence(
                                    0,
                                    diagram.fileName.length - 5
                                )
                                }_copy.spsp"
                            val destFile = File(diagram.folderPath, newName)
                            Log.i("CLONE", "${diagram.fullPath} -> ${destFile.absolutePath}")
                            File(diagram.fullPath).copyTo(destFile, overwrite = true)
                            updater()
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "copy")
                    }

                    IconButton(
                        onClick = {
                            val file = File(diagram.fullPath)
                            val deleted: Boolean = file.delete()
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

@ExperimentalMaterialApi
@Composable
fun DiagramList(diagrams: MutableList<DiagramFile>, updater: () -> Unit, context: Context) {
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(items = diagrams) { _, d ->
            DiagramCard(
                diagram = d,
                onClick = { startEditing(d.fileName, false, context) },
                updater = updater
            )
            Log.i("CARD", d.fullPath)
        }
    }
}

@Composable
fun AddButton(context: Context) {
    val nameChanger by remember { mutableStateOf(MaterialDialog()) }
    nameChanger.build {
        var oldName by remember { mutableStateOf("") }
        Row {
            OutlinedTextField(
                value = oldName,
                onValueChange = {
                    var newStr = it
                    if (newStr.length > 25)
                        newStr = newStr.slice(0..25)
                    oldName = newStr
                },
                keyboardActions = KeyboardActions(
                    onAny = { hideKeyboard(context) }
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                label = { Text("Название диаграммы") }
            )
        }
        buttons {
            negativeButton("Отмена")
            positiveButton(
                "OK",
                onClick = { startEditing(oldName, true, context) }
            )
        }
    }

    FloatingActionButton(
        backgroundColor = Y400,
        onClick = {
            nameChanger.show()
        }
    ) {
        Icon(Icons.Filled.Add, contentDescription = "add a diagram")
    }
}
