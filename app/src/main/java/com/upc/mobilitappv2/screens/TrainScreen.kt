package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.upc.mobilitappv2.screens.components.TopBar
import com.upc.mobilitappv2.sensors.SensorLoader

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TrainScreen(context: Context) {
    var sensorLoader = SensorLoader(context)
    Scaffold( topBar = { TopBar("Training") }) {
        BodyContent(sensorLoader)
    }
}

@Composable
private fun BodyContent(sensorLoader: SensorLoader){

    var capturing by remember { mutableStateOf(false) }
    var capture: Array<MutableList<FloatArray>>? by remember{ mutableStateOf(null) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                capturing = !capturing
                sensorLoader.initialize()
            },
                enabled=!capturing,
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
                capturing = !capturing
                capture=sensorLoader.stopCapture()
            },
                enabled=capturing,
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
        if (capture != null && !capturing) {
            val capture_size= capture!![0].size
            if(capture_size < 200) {
                Text("This capture is not long enough")
                Text("Size of capture: $capture_size")
            } else {
                Text("Captured succesfully")
                Text("Size of capture: $capture_size")
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    //TrainScreen()
}
