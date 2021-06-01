package com.wndenis.snipsnap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.Text
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.google.type.Date
import com.google.type.DateTime
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.color.colorChooser
import java.io.File
import java.time.LocalDateTime


const val EXTRA_MESSAGE = "com.example.myapplication.MESSAGE"
val diagrams : MutableList<DiagramFile> = mutableListOf(DiagramFile("Project1"))

data class DiagramFile(
    var name: String = "",
    var date: LocalDateTime = LocalDateTime.now(),
    var isNew: Boolean = false,
) {   }

class MenuActivity : ComponentActivity() {

    public
    override fun onCreate(savedInstanceState: Bundle?) {
        var files: Array<String> = this.fileList()
        for (fileName: String in files) {
            var file = File(fileName)
            var f = DiagramFile(file.name)
            //TODO change date
            diagrams.add(f)

           // ...
        }

        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                topBar = { TopBarMain() },
                floatingActionButton = { AddButton(diagrams) }
            ) {
                DiagramList(diagrams,this)
            }
        }
    }

}

@Composable
fun TopBarMain() {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Мои диаграммы") },
        navigationIcon = {
        },
        actions = {}
    )
}


@Composable
fun DiagramCard(
    diagram: DiagramFile,
    onClick:()-> Unit,
){
    val context = LocalContext.current
    val nameChanger = remember { MaterialDialog() }
    nameChanger.build {
        Row {
            var oldName by remember { mutableStateOf(diagram.name) }
            OutlinedTextField(
                value = oldName,
                onValueChange = {
                    var newStr = it
                    if (newStr.length > 25)
                        newStr = newStr.slice(0..25)
                    oldName = newStr
                    diagram.name = newStr
                },
                keyboardActions = KeyboardActions(
                    onAny = { hideKeyboard(context) }),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                label = { Text("Название диаграммы") })
        }
        buttons {
            negativeButton("Отмена")
            positiveButton("OK", onClick = {}) //TODO: Update table
        }
//        colorPicker()
//        colorPicker(colors = ColorPalette.Primary)
    }

    Card(
        modifier = Modifier
            .padding(
                bottom = 5.dp,
                top = 5.dp,
                start = 5.dp,
                end = 5.dp
            )
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        elevation = 16.dp,

        ) {
        Row (
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colors.surface)
        ){
 /*           Surface(
                modifier = Modifier.size(130.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                elevation = 19.dp,
                border = BorderStroke(1.dp, Color.Gray)
            ) {
            }*/

            Column(
               modifier = Modifier
                   .padding(start = 12.dp)
                   .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = diagram.name,
                    style = TextStyle(
                        fontSize = (22.sp)
                    ),
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = diagram.date.conv(),
                        style = typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            end = 25.dp
                        )
                    )
                }
            }
            IconButton(onClick = {
                nameChanger.show()
            }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "change")
                //TODO: UPDATE LIST
            }
           /* IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "import")
            }*/
            IconButton(onClick = {
                if(!diagram.isNew){
                    var destFile = File(diagram.name+"_copy")
                    destFile.createNewFile();
                    File(diagram.name).copyTo(destFile);
                    val file = File(diagram.name)
                    val deleted: Boolean = file.delete()
                }
                diagrams.add(DiagramFile(diagram.name+"_copy"));
                //TODO: UPDATE LIST
            }) {
                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "copy")
            }
            IconButton(onClick = {
                if(!diagram.isNew){
                    val file = File(diagram.name)
                    val deleted: Boolean = file.delete()
                }
              diagrams.remove(diagram);
                //TODO: UPDATE LIST
            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
            }
        }
    }
}

@Composable
fun DiagramList(diagrams: MutableList<DiagramFile>,context: Context) {
    var diagramsItems by remember { mutableStateOf(diagrams) }
    LazyColumn {
        itemsIndexed(items = diagramsItems) { index, d ->
            DiagramCard(diagram = d, onClick = {
                /**set Intent*/
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("name", d.name)
                intent.putExtra("isNew", d.isNew)
                context.startActivity(intent)

            })
        }
    }

}

@Composable
fun AddButton(diagrams: MutableList<DiagramFile>) {
    FloatingActionButton(onClick = {
        diagrams.add(DiagramFile("newDiagram",isNew = true)); }) {                 //TODO: UPDATE LIST
        Icon(Icons.Filled.Add, contentDescription = "add a diagram")
    }
}


