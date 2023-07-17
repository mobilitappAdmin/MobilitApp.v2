package com.upc.mobilitappv2.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.upc.mobilitappv2.R
import com.upc.mobilitappv2.map.Mapa
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.components.TopBar
import com.upc.mobilitappv2.ui.theme.*
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
@OptIn(ExperimentalAnimationApi::class)
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
    var location_accuracy: String? by remember { mutableStateOf("0") }
    //var mapa by remember {mutableStateOf(mapa)}
    val jardinsPedralbes = GeoPoint(41.387540, 2.117864)
    val fibPosition = GeoPoint(41.38867, 2.11196)
    var vehicleTest: String by remember { mutableStateOf("Car") }

    fun sendCO2notification(){
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context,
                context.getString(R.string.channel_id)
            )
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Journey finished") // title for notification
                .setContentText("Consum total: ${mapa.formatData(mapa.totalCO2,"CO2")}") // message for notification
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(
                        if(mapa.totalCO2 * 1000 / (mapa.totalDistance) > 45)
                            "Consum total: ${mapa.formatData(mapa.totalCO2,"CO2")}\nYour trip generated ${mapa.formatData(mapa.totalCO2, "CO2")} of CO2. You could have saved ${mapa.formatData((mapa.totalCO2 - (mapa.totalDistance * mapa.co2Table["bus"]!! / 1000)), "CO2")} of CO2 by using public transport."
                        else
                            "Consum total: ${mapa.formatData(mapa.totalCO2, "CO2")}\nCongratulations! you have potentially saved ${mapa.formatData(((mapa.totalDistance * mapa.co2Table["car"]!!/1000)-mapa.totalCO2),"CO2")} of CO2 by avoiding the use of polluting transport."

                    ))
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) { return }
            notify(0, mBuilder.build())
        }
    }

    val windowReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        // we will receive data updates in onReceive method.
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val loc: String? = intent.getStringExtra("location")
            val act: String? = intent.getStringExtra("activity")
            val fifo_str = intent.getStringExtra("fifo")
            val loc_acc = intent.getStringExtra("accuracy")
            var macro = intent.getStringExtra("macroState")
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

            if (loc_acc != null) {
                location_accuracy = loc_acc
            }

            if (fifo_str != null) {
                fifo = fifo_str
            }
            if(stop_cov!!.toDouble() >= 75.0){
                sendCO2notification()
            }

            if (macro != null) {
                macroState = macro
                //debugo
                //macro = vehicleTest
                mapa.nameToID[macro!!]?.let {
                    mapa.addMarker(GeoPoint(lastLoc[0].toDouble(),lastLoc[1].toDouble()), it,useMapPosition = false)

                    //test
                    /*mapa.trams.add(Pair("Car",1000.0))
                    mapa.trams.add(Pair("WALK",500.0))
                    mapa.trams.add(Pair("Bus",3000.0))
                    mapa.trams.add(Pair("STILL",0.0))
                    mapa.mutColor.value = ecoRed
                    mapa.totalDistance = 4500.0
                    mapa.totalCO2 = 237.0*/


                }
            }

        }
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    var popUpState: Boolean by remember { mutableStateOf(false) }
    var animationState: Boolean by remember { mutableStateOf(false) }
    var interactionSource = remember { MutableInteractionSource() }





    Box() {
        //Log.d("tram_consums","${vehiclesTrams}")
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
                        popUpState = true
                        //sendCO2notification()
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

            Text(text = "Location: " + lastLoc[0] + ", " + lastLoc[1])


            Text(text = "Predicted Activity: $macroState", fontSize = 20.sp)


            Text(text = "Activities: ,$fifo")

            //debugo button
            //Button(onClick = { vehicleTest = if(vehicleTest == "Car") "Bus" else "Car" }){Text(vehicleTest)}
                
            if (stop_cov!!.toDouble() >= 75.0) {
                sendCO2notification()
            }
            // debug text
            if (debug) {
                Spacer(modifier = Modifier.height(height = 40.dp))

                Text(text = "STOP coverage: $stop_cov %")
                Text(text = "Location error: $location_accuracy")
            }

            // When stopping capture
            if (stop != null && !multimodal.getState()) {
                var uploading: Boolean by remember { mutableStateOf(false) }
                var deleting: Boolean by remember { mutableStateOf(false) }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
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
            //mapa.debugLayout()
            mapa.newAPPLayout()


            }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = popUpState,
            enter = fadeIn(animationSpec = tween(durationMillis = 600)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
        ){
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { popUpState = false },

            ){

            }
        }

        //Pollution PopUp
        val cardHeight = 500
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(interactionSource = interactionSource, indication = null) { },
            visible = popUpState,
            enter = slideInVertically(initialOffsetY = {screenHeight.value.toInt()+(cardHeight*1.5).toInt()},animationSpec = tween(durationMillis = 1300)),
            exit = slideOutVertically(targetOffsetY = {screenHeight.value.toInt()+(cardHeight*1.5).toInt()},animationSpec = tween(durationMillis = 1000)),
        ){
            animationState  = this.transition.currentState == this.transition.targetState
            Box(
                Modifier
                    .width(screenWidth - 20.dp)
                    .height(cardHeight.dp)
                    .clip(shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp))
                    .background(if (!isSystemInDarkTheme()) Color.White else SoftGray))
            {
                IconButton(onClick = { popUpState = false},
                    Modifier
                        .background(Color.Transparent)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "close")
                }
                Box(Modifier.align(Alignment.TopCenter).size(150.dp)){
                    Image(
                        painter = painterResource(R.drawable.eco2),
                        contentDescription = "Center",
                        modifier = Modifier
                            .size(75.dp)
                            .align(Alignment.Center),
                        colorFilter = ColorFilter.tint(mapa.mutColor.value)

                    )
                }

                Column(
                    Modifier
                        .padding(top = 130.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth()){
                    Text(

                        text="CO2 consumtion: " + mapa.formatData(mapa.totalCO2,"CO2"),
                        Modifier.fillMaxWidth(),
                        color=mapa.mutColor.value,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)

                    Text(
                        text =
                            if(mapa.mutColor.value == Color(0xFFFF6D60)){
                                "Your trip generated ${mapa.formatData(mapa.totalCO2,"CO2")} of CO2. You could have saved ${mapa.formatData((mapa.totalCO2-(mapa.totalDistance * mapa.co2Table["bus"]!!/1000)),"CO2")} of CO2 by using public transport."
                            }
                            else{
                                "Congratulations! you have potentially saved  ${mapa.formatData(((mapa.totalDistance * mapa.co2Table["car"]!!/1000)-mapa.totalCO2),"CO2")} of CO2 by avoiding the use of polluting transport."

                            },
                        Modifier.padding(top = 10.dp,bottom=5.dp),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(height = 10.dp))



                    var vehiclesTrams = mapa.trams
                    if(vehiclesTrams.isEmpty()) Text("No trajects detected yet :(", textAlign = TextAlign.Center, modifier = Modifier
                        .fillMaxSize()
                        .align(
                            Alignment.CenterHorizontally
                        ))
                    else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                ,//.background(LightOrange),
                            contentPadding = PaddingValues(bottom = 40.dp),
                        ) {
                            itemsIndexed(vehiclesTrams) { index, objct ->
                                var item = objct.first
                                var dist = objct.second
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .padding(bottom = 10.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .background(mapa.getColor(item))
                                    //.background(color = LightOrange)

                                )
                                {
                                    if(item == "STILL"){
                                        Row(horizontalArrangement = Arrangement.SpaceEvenly,modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)) {
                                            Text("${item}", color = Color.White,textAlign = TextAlign.Center,modifier = Modifier)
                                        }
                                    }
                                    else{

                                        Row(horizontalArrangement = Arrangement.SpaceEvenly,modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)) {
                                            Text("${item}", color = Color.White,textAlign = TextAlign.Left,modifier = Modifier)
                                            Text("CO2 ${mapa.formatData(mapa.getCO2(dist,item), "CO2")} ", color = Color.White,textAlign = TextAlign.Left,modifier = Modifier)
                                            Text("Distance ${mapa.formatData(dist, "distance")} ", color = Color.White, textAlign = TextAlign.Right,modifier = Modifier)

                                        }
                                    }



                                }
                            }


                        }
                    }



                }
                if(mapa.trams.isNotEmpty()){
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (!isSystemInDarkTheme()) Color.White else SoftGray)
                            .padding(top = 10.dp, bottom = 5.dp, start = 20.dp, end = 20.dp)
                            .align(Alignment.BottomCenter)
                    ){
                        Text("Total distance", fontWeight = FontWeight.Bold)
                        Text(mapa.formatData(mapa.totalDistance,"distance"), fontWeight = FontWeight.Bold, textAlign = TextAlign.Right,modifier = Modifier.fillMaxWidth())
                    }
                }

            }

        }
    }
}

