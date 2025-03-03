package com.mobi.mobilitapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mobi.mobilitapp.MainActivity
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.map.Mapa
import com.mobi.mobilitapp.multimodal.Multimodal
import com.mobi.mobilitapp.screens.components.MapDialog
import com.mobi.mobilitapp.screens.components.TopBar
import com.mobi.mobilitapp.startMultimodalService
import com.mobi.mobilitapp.stopMultimodalService
import com.mobi.mobilitapp.ui.theme.*
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
    Scaffold(  topBar = { TopBar(LocalContext.current.getString(R.string.Trip)) }) {
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
    var lastWindow: String? by rememberSaveable  { mutableStateOf(multimodal.get_lastwindow()) }

    var stop_cov: String? by rememberSaveable  { mutableStateOf("0.0") }
    var ml_calls: String? by rememberSaveable  { mutableStateOf("0") }

    if (!debug && lastWindow != "-") {
        lastWindow = lastWindow!!.split(',')[0]
    }

    var fifo: String? by rememberSaveable  { mutableStateOf(multimodal.get_fifo()) }

    var macroState: String? by rememberSaveable  {
        mutableStateOf(multimodal.get_macrostate())
    }
    var stop: Boolean by rememberSaveable  { mutableStateOf(false) }
    var location_accuracy: String? by rememberSaveable  { mutableStateOf("0") }
    //var mapa by remember {mutableStateOf(mapa)}
    val jardinsPedralbes = GeoPoint(41.387540, 2.117864)
    val fibPosition = GeoPoint(41.38867, 2.11196)
    var vehicleTest: String by remember { mutableStateOf("Car") }

    // chose if the popup should put all the sub-travels with the same vehicle into one card or show them separately
    var condensedPopUp = true

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    var capturing: Boolean by rememberSaveable  { mutableStateOf(false) }
    var minipopUpState: Boolean by remember { mutableStateOf(false) }
    var popUpState: Boolean by remember { mutableStateOf(false) }
    var animationState: Boolean by remember { mutableStateOf(false) }
    var interactionSource = remember { MutableInteractionSource() }

    val res = LocalContext.current


    fun sendCO2notification(){

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(context, 114, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context,
                context.getString(R.string.channel_idHIGH)
            )
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(context.getString(R.string.JourneyFinished)) // title for notification
                .setContentText(context.getString(R.string.n1,"${mapa.formatData(mapa.totalCO2,"CO2")}")) // message for notification
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(
                        if(mapa.totalCO2 * 1000 / (mapa.totalDistance) > 45)
                            context.getString(R.string.n2,"${mapa.formatData(mapa.totalCO2,"CO2")}","${mapa.formatData(mapa.totalCO2, "CO2")}","${mapa.formatData((mapa.totalCO2 - (mapa.totalDistance * mapa.co2Table["bus"]!! / 1000)), "CO2")}")
                        else
                            context.getString(R.string.n3,"${mapa.formatData(mapa.totalCO2, "CO2")}","${mapa.formatData(((mapa.totalDistance * mapa.co2Table["car"]!!/1000)-mapa.totalCO2),"CO2")}")
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) { return }
            notify(8, mBuilder.build())
        }
    }

    class WindowReceiver: BroadcastReceiver() {

        // we will receive data updates in onReceive method.
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            Log.d("onReceive","onReceive")
            val loc: String? = intent.getStringExtra("location")
            val act: String? = intent.getStringExtra("activity")
            val fifo_str = intent.getStringExtra("fifo")
            val loc_acc = intent.getStringExtra("accuracy")
            var macro = intent.getStringExtra("macroState")
            stop_cov = intent.getStringExtra("stop")
            Log.d("STOP",stop_cov!!)
            ml_calls = intent.getStringExtra("ml")
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
            if(stop_cov!!.split(" ", ",", "%").filter { it.isNotEmpty() }[0].toDouble() >= 75.0){
                if(intent.getStringExtra("noNotify") == null) {
                    sendCO2notification()
                }
                val intent = Intent("Capture")
                intent.putExtra("Stop","")
                LocalBroadcastManager.getInstance(res).sendBroadcast(intent)
//                        context.stopMobilitAppService()
                removeReceiver()
                stop = true
                minipopUpState = true
                mapa.endTrip()
                capturing = false
            }

            if (macro != null) {
                macroState = macro
                //debugo
                //macro = vehicleTest
                mapa.nameToID[macro!!]?.let {
                    mapa.addMarker(GeoPoint(lastLoc[0].toDouble(), lastLoc[1].toDouble()), it,useMapPosition = false)

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
         fun removeReceiver(){
            LocalBroadcastManager.getInstance(res).unregisterReceiver(
                this
            )
        }
    }
    val windowReceiver = WindowReceiver()


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
                        context.startMultimodalService()
//                        multimodal.initialize()
//                        multimodal.startCapture()
                        mapa.startTrip()
                        minipopUpState = false
                        stop = false
                        capturing = true
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .width(150.dp),
                    enabled = (!capturing and !stop)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = res.getString(R.string.Start),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Text(text = res.getString(R.string.Start))
                }
                Spacer(modifier = Modifier.width(width = 40.dp))

                // Stop button
                Button(
                    onClick = {
//                        val intent = Intent("Capture")
//                        intent.putExtra("Stop","")

                        //workaround to pass it as automatic stop, because it doesnt unregister the windoReceiver properly if done as above
                        val intent = Intent("multimodal")
                        intent.putExtra("noNotify","noNotify")
                        intent.putExtra("stop","99")



                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
//                        context.stopMobilitAppService()

                        LocalBroadcastManager.getInstance(context).unregisterReceiver(windowReceiver);
                        stop = true
                        minipopUpState = true
                        mapa.endTrip()
                        capturing = false
                        //sendCO2notification()
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .width(150.dp),
                    enabled = capturing
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = res.getString(R.string.Stop),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Text(text = res.getString(R.string.Stop))
                }
            }

            Spacer(modifier = Modifier.height(height = 20.dp))
            if (lastLoc[0] != "-") {
                Text(text = lastLoc[0]+ ", " + lastLoc[1], fontSize = 15.sp)
            }


            Text(text = res.getString(R.string.pred1)+ ": $macroState", fontSize = 20.sp)


            Text(text = res.getString(R.string.pred2)+": $fifo", fontSize = 10.sp)

            //debugo button
            //Button(onClick = { vehicleTest = if(vehicleTest == "Car") "WALK" else "Car" ; mapa.selectedIcon = mapa.nameToID[vehicleTest]!! }){Text(vehicleTest)}
                
//            if (stop_cov!!.split(" ", ",", "%").filter { it.isNotEmpty() }[0].toDouble() >= 75.0) {
//                sendCO2notification()
//            }
            // debug text
            if (debug) {
                val showDialog = remember { mutableStateOf(false) }
                Text(text = "ML calls: $ml_calls.  GPS error: $location_accuracy", fontSize = 15.sp)
                Text(text = "STOP coverage: $stop_cov", fontSize = 15.sp)
                Button(
                    onClick = { showDialog.value = true },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = res.getString(R.string.pred3), fontSize = 11.sp)
                }

                if (showDialog.value) {
                    MapDialog(
                        map = multimodal.getPredictionSummary(),
                        onDismiss = { showDialog.value = false }
                    )
                }

            }

            // When stopping capture
            if (stop!!  && !capturing) {
                var uploading: Boolean by remember { mutableStateOf(false) }
                var deleting: Boolean by remember { mutableStateOf(false) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {

                    // Upload button
                    Button(
                        onClick = {
                            stop = false
                            val intent = Intent("Capture")
                            intent.putExtra("Process","Upload")
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

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
                        Text(text = res.getString(R.string.Upload))
                    }
                    // delete button
                    Button(
                        onClick = {
                            stop = false
                            val intent = Intent("Capture")
                            intent.putExtra("Process","Delete")
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C)),
                        modifier = Modifier
                            .height(40.dp)
                            .width(150.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = res.getString(R.string.Delete),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Text(text = res.getString(R.string.Delete))
                    }
                }
            }
            //mapa.debugLayout()
            mapa.appLayout()


            }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(interactionSource = interactionSource, indication = null) { },
            visible = minipopUpState,
            enter = slideInVertically(initialOffsetY = {screenHeight.value.toInt()},animationSpec = tween(durationMillis = 500)),
            exit = slideOutVertically(targetOffsetY = {screenHeight.value.toInt()},animationSpec = tween(durationMillis = 500)),
        ){
            Button(onClick = {minipopUpState = false; popUpState = true},
                colors = ButtonDefaults.buttonColors(backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray),
                contentPadding = PaddingValues(2.dp)){
                Icon(painter = painterResource(R.drawable.icons8_collapse_arrow_48) , contentDescription = "expand extra info", modifier = Modifier.size(25.dp), tint = if (!isSystemInDarkTheme()) Orange else Color.White)

            }
        }
        //darken the background behind the popup and close the it if clicked outside
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

                        text= res.getString(R.string.pred4) +" "+ mapa.formatData(mapa.totalCO2,"CO2"),
                        Modifier.fillMaxWidth(),
                        color=mapa.mutColor.value,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)

                    Text(
                        text =
                            if(mapa.mutColor.value == Color(0xFFFF6D60)){
                                context.getString(R.string.n2,"${mapa.formatData(mapa.totalCO2,"CO2")}","${mapa.formatData(mapa.totalCO2, "CO2")}","${mapa.formatData((mapa.totalCO2 - (mapa.totalDistance * mapa.co2Table["bus"]!! / 1000)), "CO2")}")
                            }
                            else{
                                context.getString(R.string.n3,"${mapa.formatData(mapa.totalCO2, "CO2")}","${mapa.formatData(((mapa.totalDistance * mapa.co2Table["car"]!!/1000)-mapa.totalCO2),"CO2")}")

                            },
                        Modifier.padding(top = 10.dp,bottom=5.dp),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(height = 10.dp))


                    var vehiclesTrams = mapa.trams
                    if(vehiclesTrams.isEmpty()) Text(res.getString(R.string.pred5), textAlign = TextAlign.Center, modifier = Modifier
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
                            if(condensedPopUp){
                                var l = mutableMapOf<String,Double>()
                                vehiclesTrams.map{
                                    if(it.first != "STILL") {
                                        if(l.contains(it.first)) l[it.first] = l[it.first]!! + it.second
                                        else l[it.first] = it.second
                                    }
                                }
                                vehiclesTrams.clear()
                                l.map{vehiclesTrams.add(Pair(it.key,it.value))}
                            }
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
                                            Text( res.getString(R.string.Distance)+ " ${mapa.formatData(dist, "distance")} ", color = Color.White, textAlign = TextAlign.Right,modifier = Modifier)

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
                        Text(res.getString(R.string.pred6), fontWeight = FontWeight.Bold)
                        Text(mapa.formatData(mapa.totalDistance,"distance"), fontWeight = FontWeight.Bold, textAlign = TextAlign.Right,modifier = Modifier.fillMaxWidth())
                    }
                }

            }

        }
    }
}

