package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Train Screen")
        Button(onClick = {
                sensorLoader.initialize()
                         },
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = "Start")
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    //TrainScreen()
}
