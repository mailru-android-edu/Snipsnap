package com.wndenis.snipsnap

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.imageloading.ImageLoadState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.wndenis.snipsnap.ui.theme.DpConst
import com.wndenis.snipsnap.ui.theme.FontConst
import com.wndenis.snipsnap.ui.theme.SnipsnapTheme
import com.wndenis.snipsnap.utils.CARD_WEIGHT
import com.wndenis.snipsnap.utils.conv
import com.wndenis.snipsnap.utils.extractName
import com.wndenis.snipsnap.utils.hideKeyboard
import com.wndenis.snipsnap.utils.makeName
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

private const val FILE_EXPORT_REQUEST_CODE = 12
private const val PICK_FILE = 2
const val MAX_FILE_NAME_LENGTH = 25

class DiagramFile(
    var fileName: String = "",
    var date: LocalDateTime = LocalDateTime.now(),
    var folderPath: String = "",
    var fullPath: String = ""
)

var FABClick: (() -> Unit)? = null
var pathToFile: String = ""

class MenuActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @OptIn(ExperimentalAnimationApi::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )
        super.onCreate(savedInstanceState)

        setContent {
            SnipsnapTheme {

                Scaffold(
                    topBar = { TopBarMain(this) },
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        val uri = data.data ?: return
        when (requestCode) {
            FILE_EXPORT_REQUEST_CODE -> exportFile(data, uri)
            PICK_FILE -> {
                val contentResolver = contentResolver
                try {
                    importDiagramFile(contentResolver, uri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /*  private fun importFile(data: Intent?, uri: Uri) {
          val contentResolver = contentResolver
          try {
              importDiagramFile(contentResolver, uri)
          } catch (e: FileNotFoundException) {
              e.printStackTrace()
          } catch (e: IOException) {
              e.printStackTrace()
          }
      }*/

    private fun importDiagramFile(
        contentResolver: ContentResolver,
        uri: Uri
    ) {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null)
                    line = addLines(stringBuilder, line, reader)
            }
        }
        Log.e("obj", stringBuilder.toString())
        // add new file
        val folder = this.filesDir
        val filename = getFileName(uri)
        val file = File(folder, filename)
        val res = file.writeText(stringBuilder.toString())
        if (filename != null) {
            startEditing(filename, false, this)
        }
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun addLines(
        stringBuilder: StringBuilder,
        line: String?,
        reader: BufferedReader
    ): String? {
        var line1 = line
        stringBuilder.append(line1)
        line1 = reader.readLine()
        return line1
    }

    private fun exportFile(data: Intent?, uri: Uri) {
        val contentResolver = contentResolver
        try {
            val file = File(pathToFile)
            val jsonRepr = file.readText()
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        jsonRepr.toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        System.exit(0)
    }
}

fun startEditing(name: String, isNew: Boolean, context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("name", name)
    intent.putExtra("isNew", isNew)
    context.startActivity(intent)
}

@Composable
fun TopBarMain(context: Context) {
    val painter = rememberGlidePainter(
        "https://picsum.photos/512/512", fadeIn = true,
        previewPlaceholder = R.drawable.splash_image
    )
    TopAppBar(
        title = { Text("Мои диаграммы") },
        navigationIcon = {
            Column(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .padding(DpConst.DST_10)
                .clip(CircleShape)
                .clickable {
                    painter.request = "https://picsum.photos/${Random.nextInt(200, 500)}"
                }) {


                when (painter.loadState) {
                    is ImageLoadState.Loading -> {
                        CircularProgressIndicator(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxSize(),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                    is ImageLoadState.Error -> {

                    }
                    is ImageLoadState.Success -> {
                        Image(
                            painter = painter,
                            contentScale = ContentScale.Crop,
                            contentDescription = "random pic",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                }
            }
        },
        actions = {

            IconButton(
                onClick = {
                    val intent2 = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/spsp"
                    }
                    startActivityForResult(context as Activity, intent2, PICK_FILE, null)
                }
            ) { Icon(imageVector = Icons.Filled.Publish, contentDescription = "export") }
        }
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
            .padding(DpConst.DST_5, DpConst.DST_5, DpConst.DST_5, DpConst.DST_5)
            .height(DpConst.DST_96)
            .clip(RoundedCornerShape(DpConst.DST_18))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(DpConst.DST_18),
        elevation = DpConst.DST_4
    ) {

        Spacer(modifier = Modifier.height(DpConst.DST_16))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(DpConst.DST_4))
                .background(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = DpConst.DST_12)
                    .align(Alignment.CenterVertically)
                    .weight(CARD_WEIGHT)
            ) {
                Text(
                    text = extractName(diagram.fileName),
                    style = TextStyle(fontSize = FontConst.FONT_18),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = diagram.date.conv(),
                        style = typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = DpConst.DST_25)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(CARD_WEIGHT)
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
                            pathToFile = diagram.fullPath
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/spsp"
                                putExtra(Intent.EXTRA_TITLE, diagram.fileName)
                                //   putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                            }
                            startActivityForResult(
                                context as Activity,
                                intent,
                                FILE_EXPORT_REQUEST_CODE,
                                null
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "export",
                            //                          tint = Color.White,
                        )
                    }
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
        Spacer(modifier = Modifier.height(DpConst.DST_16))
    }
}

@ExperimentalMaterialApi
@Composable
fun DiagramList(diagrams: MutableList<DiagramFile>, updater: () -> Unit, context: Context) {
    val nameChanger by remember { mutableStateOf(MaterialDialog()) }
    var selectedDiagram: DiagramFile? by remember { mutableStateOf(null) }
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
                .padding(DpConst.DST_18)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textFieldValueState.value,
                onValueChange = { tfv ->
                    var newStr = tfv.text
                    if (newStr.length > MAX_FILE_NAME_LENGTH)
                        newStr = newStr.slice(0..MAX_FILE_NAME_LENGTH)
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
