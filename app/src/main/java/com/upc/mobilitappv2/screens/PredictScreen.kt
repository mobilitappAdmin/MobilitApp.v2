package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.components.TopBar
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PredictScreen(context: Context, multimodal: Multimodal, preferences: SharedPreferences ) {

    val debug: Boolean? by remember { mutableStateOf(preferences.getBoolean("debug", true)) }

    Scaffold(  topBar = { TopBar("Multimodal prediction") }) {
        //Text("In development...")
        BodyContent(context, multimodal, debug!!)
    }
}

@Composable
private fun BodyContent(context: Context, multimodal: Multimodal, debug: Boolean) {

    val lastLoc = remember {
        mutableStateListOf<String>("-", "-")
    }

    var lastWindow: String? by remember { mutableStateOf("-") }

    var fifo: String? by remember { mutableStateOf("-") }

    val windowReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        // we will receive data updates in onReceive method.
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val loc: String? = intent.getStringExtra("location")
            val act: String? = intent.getStringExtra("activity")
            val fifo_str = intent.getStringExtra("fifo")
            // on below line we are updating the data in our text view.
            if (loc != null) {
                lastLoc[0] = loc.split(",").toTypedArray()[0]
                lastLoc[1] = loc.split(",").toTypedArray()[1]
            }

            if (act != null) {
                lastWindow = if (!debug) {
                    act.split(',')[0]
                } else {
                    act
                }
            }

            if (fifo_str != null) {
                fifo = fifo_str
            }

        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    // on below line we are registering our local broadcast manager.
                    LocalBroadcastManager.getInstance(context).registerReceiver(
                        windowReceiver, IntentFilter("multimodal")
                    )
                    multimodal.initialize()
                    multimodal.startCapture()
                    //lastLoc.value!!.latitude = multimodal.getLastLocation().latitude
                    //lastLoc.value!!.longitude = multimodal.getLastLocation().longitude
                },
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp),
                enabled = !multimodal.getState()
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

            Button(
                onClick = {
                    multimodal.stopCapture()
                    lastLoc[0] = "-"
                    lastLoc[1] = "-"
                },
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp),
                enabled = multimodal.getState()
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

        Text(text = "Location: "+lastLoc[0]+", "+lastLoc[1])

        Spacer(modifier = Modifier.height(height = 40.dp))

        Text(text = "Last activitiy: $lastWindow")

        Spacer(modifier = Modifier.height(height = 40.dp))

        Text(text = "Activities: $fifo")

    }

}
