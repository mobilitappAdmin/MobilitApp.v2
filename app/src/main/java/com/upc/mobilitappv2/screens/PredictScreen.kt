package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.upc.mobilitappv2.R
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.components.TopBar
import com.upc.mobilitappv2.map.Mapa
import org.osmdroid.util.GeoPoint
import java.util.*

/**
 * PredictScreen is a composable function that displays the prediction screen of the application.
 *
 * @author Gerard Caravaca and Miquel Gotanegra
 * @param context The context of the application.
 * @param multimodal The Multimodal instance used for multimodal interaction.
 * @param preferences The SharedPreferences instance used for storing preferences.
 * @param mapa The Map instance used for displaying maps.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PredictScreen(context: Context, multimodal: Multimodal, preferences: SharedPreferences, mapa: Mapa ) {

    val debug: Boolean? by remember { mutableStateOf(preferences.getBoolean("debug", true)) }
    //mapa.resetView()
    Scaffold(  topBar = { TopBar("Trip") }) {
        //Text("In development...")
        BodyContent(context, multimodal, debug!!,mapa)
    }
}


/**
 * Composable function for rendering the body content of the PredictScreen.
 *
 * @author Gerard Caravaca and Miquel Gotanegra
 * @param context The context of the application.
 * @param multimodal The Multimodal instance used for multimodal interaction.
 * @param debug Boolean value indicating whether debug mode is enabled.
 * @param mapa The Map instance used for displaying maps.
 */
@Composable
private fun BodyContent(context: Context, multimodal: Multimodal, debug: Boolean,mapa: Mapa) {

    // State variables

    val lastLoc = remember {
        mutableStateListOf(multimodal.get_lastlocation()[0], multimodal.get_lastlocation()[1])
    }
    var lastWindow: String? by remember { mutableStateOf(multimodal.get_lastwindow()) }

    var stop_cov: String? by remember { mutableStateOf("0.0") }

    if (!debug && lastWindow != "-") {
        lastWindow = lastWindow!!.split(',')[0]
    }

    var fifo: String? by remember { mutableStateOf(multimodal.get_fifo()) }

    var macroState: String? by remember {
        mutableStateOf(multimodal.get_macrostate())
    }

    var stop: Boolean? by remember { mutableStateOf(null) }
    //var mapa by remember {mutableStateOf(mapa)}
    val jardinsPedralbes = GeoPoint(41.387540, 2.117864)
    val fibPosition = GeoPoint(41.38867, 2.11196)

    val windowReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        // we will receive data updates in onReceive method.
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val loc: String? = intent.getStringExtra("location")
            val act: String? = intent.getStringExtra("activity")
            val fifo_str = intent.getStringExtra("fifo")
            val macro = intent.getStringExtra("macroState")
            stop_cov = intent.getStringExtra("stop")
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

            if (macro != null) {
                macroState = macro
                mapa.nameToID[macro!!]?.let {
                    mapa.addMarker(GeoPoint(lastLoc[0].toDouble(),lastLoc[1].toDouble()), it,useMapPosition = true)
                }
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
            // Start button
            Button(
                onClick = {
                    // on below line we are registering our local broadcast manager.
                    LocalBroadcastManager.getInstance(context).registerReceiver(
                        windowReceiver, IntentFilter("multimodal")
                    )
                    multimodal.initialize()
                    multimodal.startCapture()
                    mapa.startTrip()
                    stop = false
                },
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp),
                enabled = (!multimodal.getState())
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Text(text = "Start")
            }
            Spacer(modifier = Modifier.width(width = 40.dp))

            // Stop button
            Button(
                onClick = {
                    multimodal.stopCapture()
                    stop = true
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
                Text(text = "Stop")
            }
        }

        Spacer(modifier = Modifier.height(height = 20.dp))

        Text(text = "Location: "+lastLoc[0]+", "+lastLoc[1])


        Text(text = "Predicted Activity: $macroState", fontSize = 20.sp)


        Text(text = "Activities: $fifo")

        // debug text
        if (debug){
            Spacer(modifier = Modifier.height(height = 40.dp))

            Text(text = "Stop covering: $stop_cov %")
        }

        // When stopping capture
        if (stop != null && !multimodal.getState()) {
            var uploading: Boolean by remember { mutableStateOf(false) }
            var deleting: Boolean by remember { mutableStateOf(false) }


            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End) {
                // Upload button
                Button(
                    onClick = {
                        uploading = true
                        var upload = multimodal.pushUserInfo()
                        if (upload) {
                            stop = null
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
                        contentDescription = "Upload",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Text(text = "Upload")
                }
                // delete button
                Button(
                    onClick = {
                        deleting = true
                        var delete = multimodal.deleteUserInfo()
                        if (delete) {
                            stop = null
                            deleting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C)),
                    modifier = Modifier
                        .height(40.dp)
                        .width(150.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Text(text = "Delete")
                }
            }
        }

        mapa.APPLayout()

    }

}
