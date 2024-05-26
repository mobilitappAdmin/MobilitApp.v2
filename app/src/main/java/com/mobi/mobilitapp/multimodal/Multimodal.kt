package com.mobi.mobilitapp.multimodal

import android.Manifest
import android.R
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.gms.location.*
import com.mobi.mobilitapp.MainActivity
import com.mobi.mobilitapp.getArray
import com.mobi.mobilitapp.helper.formatDate
import com.mobi.mobilitapp.helper.indrawDays
import com.mobi.mobilitapp.saveArray
import com.mobi.mobilitapp.sensors.SensorLoader
import com.mobi.mobilitapp.server.UploadService
import com.mobi.mobilitapp.stopMultimodalService
import org.osmdroid.util.GeoPoint
import java.io.File
import java.lang.Math.abs
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.cos
import kotlin.math.sqrt


/**
 * This class represents the Multimodal service that captures location data, sensor data, and provides activity analysis.
 *
 * @author Gerard Caravaca
 * @param context The context of the application.
 * @param sensorLoader An instance of SensorLoader used to load and analyze sensor data.
 * @param preferences The SharedPreferences instance to access user preferences.
 */
class Multimodal: Service() {

    //constructor
    private var context: Context = this
    private lateinit var sensorLoader:SensorLoader
    //private lateinit var notificationId: Int
    private lateinit var notification: Notification
    private lateinit var user_id: String
    private lateinit var preferences: SharedPreferences

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var mlService: MLService
    private lateinit var stopService: StopService
    private var fifoAct: LinkedList<String> = LinkedList<String>()
    private var last_accuracies: LinkedList<Int> = LinkedList<Int>()
    private var predictionSummary: MutableMap<String, Int> = mapOf(
        "Bus" to 0,
        "Car" to 0,
        "E-Scooter" to 0,
        "Metro" to 0,
        "Run" to 0,
        "STILL" to 0,
        "Train" to 0,
        "Tram" to 0,
        "WALK" to 0
    ) as MutableMap<String, Int>

    private var locations = ArrayList<Location>()
    private var last_distance: Double = 0.0

    private var capturing = false
    private var macroState = "STILL"
    private var prevMacroState = "STILL"
    private var captureHash: Int = 0
    private var othersRow: Int = 0
    private var ml_calls: Int = 0
    private var stop: Pair<Float, Boolean> = Pair(0.0f, false)

    private lateinit var startDate: Date
    private lateinit var drawDate: Date // nomes per el sorteig
    private var startLoc: Location? = null
    private var first: Boolean = true
    private var TAG = "TESTING"

    private var lastwindow = "-"

    private lateinit var  userInfoService: UserInfo

    val FILEPATH = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath + "/MobilitAppV2/sensors"

    //comunication

    private val updateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        // we will receive data updates in onReceive method.
        override fun onReceive(context: Context?, intent: Intent) {
            if(intent.hasExtra("Stop")) stopCapture()
            else if(intent.hasExtra("Process")){
                if(intent.getStringExtra("Process")!! == "Upload"){
                    pushUserInfo()
                }
                else{
                    deleteUserInfo()
                }

            }

        }
    }

    /**
     * Returns the last captured location as an array of longitude and latitude coordinates.
     *
     * @return An array containing the longitude and latitude of the last captured location.
     */
    fun get_lastlocation(): Array<String> {
        if (locations.size > 0){
            return arrayOf(
                locations[locations.size - 1].longitude.toString(),
                locations[locations.size - 1].latitude.toString()
            )
        }
        else {
            return arrayOf("-", "-")
        }

    }
    fun setSensorLoader(sensor: SensorLoader){
        sensorLoader = sensor
        Log.d("SensorLoader", "Sensor loader set")
    }

    /**
     * Returns the last captured activity analysis window.
     *
     * @return The last captured activity analysis window as a string.
     */
    fun get_lastwindow(): String {
        return lastwindow
    }

    /**
     * Returns the FIFO (First In, First Out) activity analysis window.
     *
     * @return The FIFO activity analysis window as a string.
     */
    fun get_fifo(): String {
        if (fifoAct.size > 0) {
            var fifoStr = ""
            for (a in fifoAct) {
                if (a != "-") {
                    if (fifoStr == "") {
                        fifoStr = "$fifoStr$a"
                    } else {
                        fifoStr = "$fifoStr, $a"
                    }
                }
            }
            return fifoStr
        }
        else {
            return "-"
        }
    }

    /**
     * Returns the current macrostate.
     *
     * @return The current macrostate as a string.
     */
    fun get_macrostate(): String {
        return macroState
    }


    /**
     * Initializes the Multimodal service by setting up required components and services.
     */
    fun initialize() {

        //needed to retrieve EncryptedSharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        // Initialize/open an instance of EncryptedSharedPreferences on below line.
        preferences = EncryptedSharedPreferences.create(
            // passing a file name to share a preferences
            "preferences",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        Log.d(TAG,"Shared Preferences Gender ${preferences.getString("gender", ":(")}")
        first = true
        startDate = Date()
        drawDate = Date()
        val ANDROID_ID: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        captureHash = abs((startDate.toString()+ANDROID_ID).hashCode())
        stop = Pair(0.0f, false)
        val email = preferences.getString("email", "False")
        userInfoService =  UserInfo(FILEPATH, captureHash.toString() + '_' + "UserInfo.csv")
        //SORTEIG
//        userInfoService = if (email != null && email != "False") {
//            UserInfo(FILEPATH, captureHash.toString()+"_"+email+"_"+"UserInfo.csv")
//        } else {
//            UserInfo(FILEPATH, captureHash.toString() + '_' + "UserInfo.csv")
//        }
        mlService =  MLService(context)
        mlService.initialize() //load Model
        stopService = StopService(alpha = preferences.getFloat("alpha",0f).toDouble(), max_radium = 30, num_points = 90, covering_threshold = 75.0F)
        stopService.initialize()
        fifoAct = LinkedList<String>()
        last_accuracies = LinkedList<Int>()
        locations = ArrayList<Location>()
        last_distance = 0.0
        othersRow = 0
        ml_calls = 0
        macroState = "STILL"
        prevMacroState = "STILL"
        lastwindow = "-"
        predictionSummary = mapOf(
            "Bus" to 0,
            "Car" to 0,
            "E-Scooter" to 0,
            "Metro" to 0,
            "Run" to 0,
            "STILL" to 0,
            "Train" to 0,
            "Tram" to 0,
            "WALK" to 0
        ) as MutableMap<String, Int>


        sensorLoader= SensorLoader(application.applicationContext,Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))

        Log.d(TAG, "Service Intialized")

        val intent = Intent("multimodal")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.locations[locationResult.locations.size - 1]
                val accuracy = location.accuracy


                if (location != null) {
                    if (startLoc == null) {
                        startLoc = location
                    }
                    locations.add(location)
                    var activity = sensorLoader.analyseLastWindow().toString()
                    if (activity!="-") {activity = activity+"last distance: "+last_distance.toString()}

                    if (activity.split(',')[0] == "MOVING") {
                        if (isStill(accuracy)) {
                            activity = "STILL,"+activity.split(',')[1] //HERE
                        }
                    }

                    lastwindow = activity

                    fifoAct.add(activity.split(',')[0])
                    if (fifoAct.size > 5) {
                        fifoAct.removeFirst()
                    }
                    var fifoStr = ""
                    for (a in fifoAct){
                        if (a != "-") {
                            if (fifoStr == "") {
                                fifoStr = "$fifoStr$a"
                            } else {
                                fifoStr = "$fifoStr, $a"
                            }
                        }
                    }

                    if (othersRow != 0) {
                        if ((othersRow-1)%3 == 0 && (othersRow-1) != 0)  { // Call ML periodically
                            val (prediction, summary) =
                                mlService.overallPrediction(sensorLoader.getLastWindow(fifoAct.size, fifoAct))
                            macroState = prediction
                            predictionSummary = summary
                            ++ml_calls
                        }
                        ++othersRow
                    }


                    var majority = majorityState()
                    if (majority == null) {
                        majority = macroState
                    }
                    if (majority == "MOVING") {
                        if (othersRow == 0) {
                            val (prediction, summary) =
                                mlService.overallPrediction(sensorLoader.getLastWindow(3, fifoAct))
                            macroState = prediction
                            predictionSummary = summary


                            ++othersRow
                            ++ml_calls
                        }
                    }
                    else {
                        macroState = majority
                        othersRow = 0
                        ml_calls = 0
                    }
                    // STOP algorithm
                    if (accuracy < 100) {
                        stop = stopService.addLocation(location)
                    }
                    Log.d(TAG, "Location received")
                    intent.putExtra("macroState", macroState)
                    intent.putExtra("fifo", fifoStr)
                    intent.putExtra("activity", activity)
                    intent.putExtra("accuracy", accuracy.toString())
                    intent.putExtra("location", location.latitude.toString()+","+location.longitude.toString())
                    intent.putExtra("stop", BigDecimal(stop.first.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toString() + " %, " + stopService.get_size().toString() + ", " +
                            BigDecimal(stopService.distance_to_last_location()).setScale(2, RoundingMode.HALF_EVEN).toString() + " m, " +  BigDecimal(stopService.get_current_alpha()).setScale(3, RoundingMode.HALF_EVEN).toString())
                    intent.putExtra("ml", ml_calls.toString())
                    Log.d(TAG, "Send fifo $fifoStr")
                    Log.d(TAG, "Capturing $capturing")

                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                    if (macroState != prevMacroState) {
                        if (!first) {
                            //userinfo
                            val organization = ""
                            // SORTEIG
//                            organization = preferences.getString("organization", null)!!,
                            userInfoService.createUserInfoDataFile(
                                captureHash,
                                preferences.getString("gender", "null")!!,
                                preferences.getString("age", "null")!!,
                               organization,
                                preferences.getString("role", "null")!!,
                                preferences.getString("grade", "null")!!,
                                prevMacroState,
                                arrayOf(
                                    startLoc!!.longitude.toString(),
                                    startLoc!!.latitude.toString()
                                ),
                                arrayOf(
                                    location.longitude.toString(), location.latitude.toString()
                                ),
                                startDate.toString(),
                                Date().toString()
                            )
                        }
                        first = false
                        startDate = Date()
                        startLoc = location
                        prevMacroState = macroState
                    }
                    if (stop.second) {
                        //stopCapture()
                        stopForegroundService()
                    }
                }
            }
        }

        locationRequest = LocationRequest.create()
        locationRequest.interval = (30 * 1000).toLong() // 18 seconds
        locationRequest.fastestInterval = (20 * 1000).toLong() // 16 seconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        Log.d(TAG, "Setup Location Updates")

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand")

        getConstructorParams(intent)
        initialize()

        //recieve updates from app
        LocalBroadcastManager.getInstance(context).registerReceiver(
            updateReceiver, IntentFilter("Capture")
        )

        startAsForegroundService()
        startCapture()

        Log.d(TAG, "Service Started")
        Log.d(TAG, "capturing ${capturing}")

        return START_REDELIVER_INTENT
    }
    private fun getConstructorParams(intent: Intent) {
        user_id = intent.getStringExtra("userId").toString()
        val NotificationTitle = intent.getStringExtra("NotificationTitle")
        val NotificationContent = intent.getStringExtra("NotificationDescription")
        val NotificationChannel = intent.getStringExtra("NotificationChannel")

        if (NotificationTitle != null && NotificationContent != null && NotificationChannel != null) {
            notification =
                createNotification(NotificationTitle, NotificationContent, "Mobilitapp")!!
        }
        else {
            notification =
                createNotification("Mobilitapp", "Foreground service developed", "Mobilitapp")!!
        }
    }
    private fun startAsForegroundService() {
        // promote service to foreground service
        startForeground(1, notification)

        Log.d(TAG, "Start Foreground")
    }
    fun stopForegroundService() {
        stopSelf()
    }
    private fun createNotification(title: String, content: String, channel_id: String): Notification? {
        // Build your notification here using the NotificationCompat.Builder
        // Don't forget to set a small icon, or the notification will not show

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(context, 8008, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(com.mobi.mobilitapp.R.mipmap.ic_launcher) // notification icon
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        return builder.build()
    }

    fun getPredictionSummary(): MutableMap<String, Int> {
        return predictionSummary
    }

    private fun majorityState(): String? {
        if (fifoAct.size >= 3) {
            var majority: String? = null
            val activities = mutableMapOf(
                "STILL" to 0,
                "WALK" to 0,
                "MOVING" to 0
            )
            var counts = 0
            for (state in fifoAct) {
                if (state in activities.keys) {
                    counts++
                    activities[state] = activities[state]!! + 1
                }
            }
            if (counts >= 3) {
                majority = activities.maxBy { it.value }.key
            }
            if (majority != null){
                if ((majority == "MOVING" && activities["MOVING"]!! >= 3) || majority != "MOVING") {
                    if (majority == "STILL") {
                        return if (fifoAct[fifoAct.size - 1] == fifoAct[fifoAct.size - 2] && fifoAct[fifoAct.size - 2] == fifoAct[fifoAct.size - 3] && fifoAct[fifoAct.size - 1] == "STILL") {
                            majority
                        } else {
                            null
                        }
                    }
                    return majority
                }
            }
        }
        return null
    }

    /**
     * Computes the distance between two geographic coordinates using the Haversine formula.
     *
     * @param lat1 Latitude of the first coordinate.
     * @param lon1 Longitude of the first coordinate.
     * @param lat2 Latitude of the second coordinate.
     * @param lon2 Longitude of the second coordinate.
     * @return The distance between the two coordinates in meters.
     */
    private fun computeDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        var lon2 = lon2
        val radius = 6371.0
        lat1 = lat1 * Math.PI / 180
        lat2 = lat2 * Math.PI / 180
        lon1 = lon1 * Math.PI / 180
        lon2 = lon2 * Math.PI / 180
        val deltaLat = lat1 - lat2
        val deltaLon = lon1 - lon2
        val x = deltaLon * cos((lat1 + lat2) / 2)
        return radius * sqrt(x * x + deltaLat * deltaLat) * 1000
    }

    /**
     * Checks if the device is still based on the recent recorded locations.
     * The device is considered still if the distance between the last two recorded locations is less than 20 meters.
     *
     * @return True if the device is still, false otherwise.
     */
    fun isStill(accuracy: Float): Boolean {
        if (locations.size >= 2 && accuracy < 100){
            val loc0 = locations[locations.size-2]
            val loc1 = locations[locations.size-1]
            val dist = computeDistance(loc0.latitude, loc0.longitude, loc1.latitude, loc1.longitude)
            last_distance = dist

            return dist < 20
        }
        return false
    }

    /**
     * Starts capturing location updates and sensor data.
     * Requires the ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
     */
    fun startCapture() {

        capturing = true
        // Request location permissions if not granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        // Start receiving location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        // Initialize sensor data capture
        sensorLoader.initialize("Multimodal")
        Log.d(TAG, "Start location updates")
    }

    /**
     * Retrieves the last recorded location.
     *
     * @return The last recorded location.
     */
    fun getLastLocation(): Location {
        return locations.last()
    }

    /**
     * Retrieves the current state of the capture process.
     *
     * @return True if the capture process is ongoing, false otherwise.
     */
    fun getState(): Boolean {
        return capturing
    }

    /**
     * Stops capturing location updates and sensor data.
     */
    fun stopCapture() {
        capturing = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorLoader.stopCapture()
        Log.d(TAG,"Capture finished")
        //push server
        if (!first) {
            val organization = ""
            // SORTEIG
//          organization = preferences.getString("organization", null)!!,
            userInfoService.createUserInfoDataFile(
                captureHash,
                preferences.getString("gender", "null")!!,
                preferences.getString("age", "null")!!,
                organization,
                preferences.getString("role", "")!!,
                preferences.getString("grade", "")!!,
                macroState,
                arrayOf(
                    startLoc!!.longitude.toString(),
                    startLoc!!.latitude.toString()
                ),
                arrayOf(
                    locations[locations.size - 1].longitude.toString(),
                    locations[locations.size - 1].latitude.toString()
                ),
                startDate.toString(),
                Date().toString()
            )
        }
    }
    override fun onCreate() {
        super.onCreate()

        //Toast.makeText(this, "Foreground Service created", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Service Created")
    }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(
            updateReceiver)
        Log.d(TAG, "OnDestroy")
    }

    fun pushUserInfo(): Boolean {
        // SORTEIG
//        if (checkDrawValidity()) {
//            val email = preferences.getString("email", "")
//            if (email != "" && email != "False") {
//                // save the day
//                if (indrawDays(startDate)) {
//                    val today = formatDate(startDate)
//                    var daysArray = getArray("draw", preferences)
//                    if (daysArray != null) {
//                        if (!daysArray.contains(today)){
//                            daysArray += today
//                            saveArray(daysArray, "draw", preferences)
//                        }
//                    }
//                    else {
//                        saveArray(arrayOf(today), "draw", preferences)
//                    }
//                }
//            }
//        }
        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("USERINFO", "Uploading...")
        context.startService(intent)
        context.stopMultimodalService()
        return true
    }
    private fun checkDrawValidity(): Boolean {
        if (locations.size >= 2) {
            val dist = computeDistance(
                locations[0].latitude,
                locations[0].longitude,
                locations[locations.size - 1].latitude,
                locations[locations.size - 1].longitude
            )
            val time = differenceInMinutes(drawDate, Date())
            Log.d("VIATGE", "$dist metres i $time minuts")

            return dist >= 400f && time >= 4f
        }
        return false
    }

    private fun differenceInMinutes(date1: Date, date2: Date): Float {
        val diffInMillis = date2.time - date1.time
        return diffInMillis / (60f * 1000f)
    }


    /**
     * Delete userinfo files
     *
     * @return True if files were deleted.
     */
    fun deleteUserInfo(): Boolean {
        thread {
            Log.d("USERINFO", "Deleting userinfo files")

            val FILEPATH = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .absolutePath + "/MobilitAppV2/sensors"

            try {
                val dir = File(FILEPATH)
                val files = dir.listFiles()
                if (files != null) {
                    Log.d("DELETE", "Number of files: " + files.size)
                }
                if (files != null) {
                    for (i in files.indices) {
                        if (files[i].isFile) {
                            val deleteFile = File(files[i].toString())
                            val delete = deleteFile.delete()
                            if (delete) {
                                Log.d(
                                    "DELETE", """The file ${files[i]} has been deleted"""
                                )
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("DELETE", e.toString())
            }
        }
        context.stopMultimodalService()
        return true
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


}