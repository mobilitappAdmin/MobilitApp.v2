package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.components.TopBar
import java.util.*

private val lastLoc: MutableState<Location?> = mutableStateOf(Location("0,0"))

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PredictScreen(multimodal: Multimodal) {
    Scaffold(    topBar = { TopBar("Multimodal prediction") }) {
        BodyContent(multimodal)
    }
}

@Composable
private fun BodyContent(multimodal: Multimodal) {
    Button(onClick = {
        multimodal.initialize()
        multimodal.startCapture()
        lastLoc.value!!.latitude = multimodal.getLastLocation().latitude
        lastLoc.value!!.longitude = multimodal.getLastLocation().longitude
    },
        modifier= Modifier
            .height(40.dp)
            .width(150.dp)
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = "Start",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = "Start")
    }
    if (lastLoc.value != null) {
        Text(text = "Last location: lat: " + lastLoc.value!!.latitude +  ", lon: " + lastLoc.value!!.longitude)
    }
    else {
        Text(text = "-")
    }

    /*
    Button(onClick = {
        multimodal.stopCapture()
    },
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

     */
}
