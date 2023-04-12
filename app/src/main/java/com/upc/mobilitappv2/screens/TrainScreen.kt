package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.upc.mobilitappv2.screens.components.TopBar
import com.upc.mobilitappv2.sensors.SensorLoader
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TrainScreen(context: Context, sensorLoader: SensorLoader) {

    Scaffold( topBar = { TopBar("Training") }) {
        BodyContent(sensorLoader)
    }
}

@Composable
private fun BodyContent(sensorLoader: SensorLoader){
    val simpleDateFormat = SimpleDateFormat("yyyy-LL-dd HH:mm:ss")
    var startTime: String? by remember { mutableStateOf(sensorLoader.getTimes()[0]) }
    var endTime: String? by remember { mutableStateOf(sensorLoader.getTimes()[1]) }
    var capturing by remember { mutableStateOf(sensorLoader.getState()) }
    var capture: Int? by remember{ mutableStateOf(sensorLoader.getCapture()) }
    var openDialog: Boolean by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActivityDialog(showDialog = openDialog, dismissDialog = {
            openDialog=false
            capturing=sensorLoader.getState()
            if (capturing) {
                startTime = sensorLoader.getTimes()[0]
            }
                                                                }
            , sensorLoader)
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                startTime=simpleDateFormat.format(Date())
                openDialog=true
            },
                enabled=!sensorLoader.getState(),
                modifier= Modifier
                    .height(40.dp)
                    .width(150.dp)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = "Start")
            }
            Spacer(modifier = Modifier.width(width = 40.dp))
            Button(onClick = {
                capture=sensorLoader.stopCapture()
                endTime=simpleDateFormat.format(Date())
                capturing=false
            },
                enabled=sensorLoader.getState(),
                modifier= Modifier
                    .height(40.dp)
                    .width(150.dp)
            ) {
                Icon(
                    Icons.Filled.Done,
                    contentDescription = "Stop",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = "Stop")
            }
        }
        Spacer(modifier = Modifier.height(height = 40.dp))
        if (capture != null && !capturing && !sensorLoader.getUploadState()) {
            val capture_size= sensorLoader.finishedCapture
            if(capture_size!! < 200) {
                Text("This capture is not long enough")
                Text("Size of capture: $capture_size")
                val currentTime=endTime
                Text(text = "Start: $startTime")
                Text(text="End: $currentTime")
            } else {
                Text("Captured succesfully")
                Text("Size of capture: $capture_size")
                val currentTime=endTime
                Text(text = "Start: $startTime")
                Text(text = "End: $currentTime")
                Spacer(modifier = Modifier.height(height = 40.dp))
                var uploading: Boolean by remember { mutableStateOf(false) }
                var deleting: Boolean by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            uploading = true
                            var upload = sensorLoader.saveCapture()
                            if (upload) {
                                capture = null
                                uploading = false
                            }
                        },
                        modifier = Modifier
                            .height(40.dp)
                            .width(150.dp)
                    ) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = "Upload",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Upload")
                    }
                    Spacer(modifier = Modifier.width(width = 40.dp))
                    Button(
                        onClick = {
                            deleting = true
                            var delete = true
                            if (delete) {
                                capture = null
                                deleting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C))
                        ,
                        modifier = Modifier
                            .height(40.dp)
                            .width(150.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Delete")
                    }
                }
                }
        } else if (sensorLoader.getState()){
            Text("Capturing...")
        }
    }

}

@Composable
private fun ActivityDialog(
    showDialog: Boolean,
    dismissDialog: ()->Unit,
    sensorLoader: SensorLoader
){
    if (showDialog){
        var activity: String? = null
        AlertDialog(
            onDismissRequest = {},
            title = {Text("Select an activity:", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))},
            confirmButton = {
                            Button(onClick = {
                                sensorLoader.initialize(activity!!)
                                dismissDialog()
                            }) {
                                Text("Accept")
                            }
            },
            dismissButton = {
                Button(onClick = { dismissDialog() }) {
                    Text("Cancel")
                }
            },
            text = {
                val radioOptions = listOf("Bus", "Metro", "Train", "Car", "Bici", "e-Bicing", "Moto", "Tram", "Run", "Walk", "e-Scooter")
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[2]) }
                activity=selectedOption
                Column(
                    modifier= Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) }
                                )
                                .padding(horizontal = 16.dp)
                        ){
                            RadioButton(
                                selected = (text == selectedOption),
                                modifier = Modifier.padding(all = Dp(value = 2F)),
                                onClick = {
                                    onOptionSelected(text)
                                    activity = text
                                })
                            Text(
                                fontSize = 18.sp,
                                text = text,
                                modifier = Modifier.padding(start = 2.dp, top=16.dp)
                            )

                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    //TrainScreen()
}
