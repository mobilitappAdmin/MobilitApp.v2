package com.upc.mobilitappv2.multimodal

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.upc.mobilitappv2.sensors.SensorLoader
import com.upc.mobilitappv2.server.UploadService
import java.io.File
import java.lang.Math.abs
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
class Multimodal(private val context: Context, private val sensorLoader: SensorLoader, private val preferences: SharedPreferences): Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var mlService: MLService
    private lateinit var stopService: StopService
    private var fifoAct: LinkedList<String> = LinkedList<String>()

    private var locations = ArrayList<Location>()
    private var last_distance: Double = 0.0

    private var capturing = false
    private var macroState = "STILL"
    private var prevMacroState = "STILL"
    private var captureHash: Int = 0
    private var othersRow: Int = 0

    private lateinit var startDate: Date
    private var startLoc: Location? = null
    private var first: Boolean = true

    private var lastwindow = "-"

    private lateinit var  userInfoService: UserInfo

    val FILEPATH = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath + "/MobilitAppV2/sensors"

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
                if (fifoStr == "") {
                    fifoStr = "$fifoStr$a "
                } else {
                    fifoStr = "$fifoStr,$a "
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
        first = true
        startDate = Date()
        captureHash = abs(startDate.hashCode())
        userInfoService = UserInfo(FILEPATH, captureHash.toString()+'_'+"UserInfo.csv")
        mlService =  MLService(context)
        mlService.initialize() //load Model
        stopService = StopService(alpha = 0.05, max_radium = 30, num_points = 90, covering_threshold = 75.0F) // CHANGE
        stopService.initialize()
        fifoAct= LinkedList<String>()
        locations = ArrayList<Location>()
        last_distance = 0.0
        othersRow = 0
        macroState = "STILL"
        prevMacroState = "STILL"
        lastwindow = "-"

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
                            activity = "STILL,"+activity.split(',')[1]
                        }
                    }

                    lastwindow = activity

                    fifoAct.add(activity.split(',')[0])
                    if (fifoAct.size > 3) {
                        fifoAct.removeFirst()
                    }
                    var fifoStr = ""
                    for (a in fifoAct){

                        if (fifoStr =="") {fifoStr = "$fifoStr$a "}
                        else {fifoStr = "$fifoStr,$a " }
                    }

                    if (othersRow != 0) {
                        if (othersRow == 4 || othersRow == 10) {
                            val prediction =
                                mlService.overallPrediction(sensorLoader.getLastWindow(6))
                            macroState = prediction
                        }
                        ++othersRow

                    }

                   // Windows logic
                    if (fifoAct.size == 3) {
                        if (fifoAct[1] == fifoAct[2] && fifoAct[2] == "WALK"){
                            othersRow = 0
                            macroState = "WALK"
                        }
                        else if (fifoAct[0] == fifoAct[1] && fifoAct[1] == fifoAct[2]){
                            if (fifoAct[2] == "MOVING") {
                                if (othersRow == 0) {
                                    val prediction =
                                        mlService.overallPrediction(sensorLoader.getLastWindow(3))
                                    macroState = prediction
                                    ++othersRow
                                }
                            }
                            else if (fifoAct[2] == "STILL") {
                                othersRow = 0
                                macroState = "STILL"
                            }
                        }
                    }

                    // STOP algorithm
                    val stop = stopService.addLocation(location)

                    intent.putExtra("macroState", macroState)
                    intent.putExtra("fifo", fifoStr)
                    intent.putExtra("activity", activity)
                    intent.putExtra("accuracy", accuracy.toString())
                    intent.putExtra("location", location.latitude.toString()+","+location.longitude.toString())
                    intent.putExtra("stop", stop.first.toString())
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                    if (macroState != prevMacroState) {
                        if (!first) {
                            //userinfo
                            userInfoService.createUserInfoDataFile(
                                captureHash,
                                preferences.getString("gender", null)!!,
                                preferences.getString("age", null)!!,
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
                        stopCapture()
                    }
                }
            }
        }

        locationRequest = LocationRequest.create()
        locationRequest.interval = (18 * 1000).toLong() // 18 seconds
        locationRequest.fastestInterval = (16 * 1000).toLong() // 16 seconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

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
        //push server
        if (!first) {
            userInfoService.createUserInfoDataFile(
                captureHash,
                preferences.getString("gender", null)!!,
                preferences.getString("age", null)!!,
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

    fun pushUserInfo(): Boolean {
        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("USERINFO", "Uploading...")
        context.startService(intent)

        return true
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
        return true
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


}