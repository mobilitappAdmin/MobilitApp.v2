package com.mobi.mobilitapp.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.screens.components.TopBar
import com.mobi.mobilitapp.sensors.SensorLoader
import com.mobi.mobilitapp.ui.theme.Orange
import com.mobi.mobilitapp.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.*


/**
 * Composable function representing the TrainScreen.
 *
 * @author Gerard Caravaca
 * @param sensorLoader The instance of SensorLoader.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TrainScreen(sensorLoader: SensorLoader) {

    Scaffold( topBar = { TopBar(LocalContext.current.getString(R.string.GenerateData)) }) {
        BodyContent(sensorLoader)
    }
}

/**
 * Composable function representing the body content of the TrainScreen.
 *
 * @author Gerard Caravaca
 * @param sensorLoader The instance of SensorLoader.
 */
@Composable
private fun BodyContent(sensorLoader: SensorLoader){
    val simpleDateFormat = SimpleDateFormat("yyyy-LL-dd HH:mm:ss")
    var startTime: String? by remember { mutableStateOf(sensorLoader.getTimes()[0]) }
    var endTime: String? by remember { mutableStateOf(sensorLoader.getTimes()[1]) }
    var capturing by remember { mutableStateOf(sensorLoader.getState()) }
    var capture: Int? by remember{ mutableStateOf(sensorLoader.getCapture()) }
    var openDialog: Boolean by remember { mutableStateOf(false) }
    val res = LocalContext.current
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
                    contentDescription = res.getString(R.string.Start),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = res.getString(R.string.Start))
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
                    contentDescription = res.getString(R.string.Stop),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = res.getString(R.string.Stop))
            }
        }
        Spacer(modifier = Modifier.height(height = 40.dp))
        if (capture != null && !capturing && !sensorLoader.getUploadState()) {
            val capture_size = sensorLoader.finishedCapture
            if(capture_size!! < 512) {
                Text(res.getString(R.string.train1))
                Text(res.getString(R.string.train2)+": $capture_size")
                val currentTime=endTime
                Text(text = res.getString(R.string.Started)+": $startTime")
                Text(text=res.getString(R.string.Finished)+": $currentTime")
            } else {
                Text(res.getString(R.string.train3))
                Text(res.getString(R.string.train2)+": $capture_size")
                val currentTime=endTime
                Text(text = res.getString(R.string.Started)+": $startTime")
                Text(text=res.getString(R.string.Finished)+": $currentTime")
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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5DFB56)),
                        modifier = Modifier
                            .height(40.dp)
                            .width(150.dp)
                    ) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = res.getString(R.string.Upload),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = res.getString(R.string.Upload))
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
                            contentDescription = res.getString(R.string.Delete),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = res.getString(R.string.Delete))
                    }
                }
                }
        } else if (sensorLoader.getState()){
            Text(res.getString(R.string.train4)+"...")
        }
    }

}

@Composable
private fun ActivityDialog(
    showDialog: Boolean,
    dismissDialog: ()->Unit,
    sensorLoader: SensorLoader
){
    val res = LocalContext.current
    val translationTable = mapOf( res.getString(R.string.Bicycle) to "Bicycle",
        res.getString(R.string.Bus) to "Bus",
        res.getString(R.string.Car) to "Car",
        res.getString(R.string.e_Bicycle) to "e_Bicycle",
        res.getString(R.string.e_Scooter) to "e_Scooter",
        res.getString(R.string.Metro) to "Metro",
        res.getString(R.string.Moto ) to "Moto",
        res.getString(R.string.Run) to "Run",
        res.getString(R.string.Stationary) to "Still",
        res.getString(R.string.Train) to "Train",
        res.getString(R.string.Tram) to "Tram",
        res.getString(R.string.Walk) to "Walk")
    if (showDialog){
        var activity: String? = null
        AlertDialog(
            backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
            onDismissRequest = {},
            title = {Text(res.getString(R.string.train5)+":", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))},
            confirmButton = {
                            TextButton(onClick = {
                                sensorLoader.initialize(activity!!)
                                dismissDialog()
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Transparent,
                            )) {
                                Text(text = res.getString(R.string.Accept),color = Orange,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                            }
            },
            dismissButton = {
                TextButton(onClick = { dismissDialog() }) {
                    Text(text = res.getString(R.string.Deny),color = Orange,style = TextStyle(fontSize = 16.sp))
                }
            },
            text = {
                val radioOptions = listOf( res.getString(R.string.Bicycle),
                                            res.getString(R.string.Bus),
                                            res.getString(R.string.Car),
                                            res.getString(R.string.e_Bicycle),
                                            res.getString(R.string.e_Scooter),
                                            res.getString(R.string.Metro),
                                            res.getString(R.string.Moto),
                                            res.getString(R.string.Run),
                                            res.getString(R.string.Stationary),
                                            res.getString(R.string.Train),
                                            res.getString(R.string.Tram),
                                            res.getString(R.string.Walk))

                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[2]) }
                activity=translationTable[selectedOption]
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
                                    activity = translationTable[text]
                                    Log.d("activity",activity!!)
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
