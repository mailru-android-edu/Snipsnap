package com.wndenis.snipsnap

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.Text
import androidx.compose.runtime.saveable.rememberSaveable
import 	android.widget.Toast
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import java.time.LocalDateTime


const val EXTRA_MESSAGE = "com.example.myapplication.MESSAGE"

data class Diagram(
    val name: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val events: MutableList<CalendarSection> = mutableListOf()
) {   }

class MenuActivity : ComponentActivity() {
    val diagrams : MutableList<Diagram> = mutableListOf(Diagram("Project1"))
    public
    override fun onCreate(savedInstanceState: Bundle?) {
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

    fun goToDiagram() {
        val message = "aaa"
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
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
    diagram: Diagram,
    onClick:()-> Unit,
){
    Card(
        modifier = Modifier
            .padding(
                bottom = 5.dp,
                top = 5.dp,
                start = 5.dp,
                end = 5.dp
            )
            .fillMaxWidth()
            .clickable(onClick = onClick)
        ,
        shape =  RoundedCornerShape(19.dp),
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
                Text(text = diagram.name,
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
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "change")
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "import")
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "copy")
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
            }
        }
    }
}

@Composable
fun DiagramList(diagrams: MutableList<Diagram>,context: Context) {
    var diagramsItems by remember { mutableStateOf(diagrams) }
    LazyColumn {
        itemsIndexed(items = diagramsItems) { index, d ->
            DiagramCard(diagram = d, onClick = {
                /**set Intent*/
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("name", d.name)
                context.startActivity(intent)

            })
        }
    }

}




@Composable
fun AddButton(diagrams: MutableList<Diagram>) {
    FloatingActionButton(onClick = { diagrams.add(Diagram("work2")); }) {
        Icon(Icons.Filled.Add, contentDescription = "add a diagram")
    }
}


