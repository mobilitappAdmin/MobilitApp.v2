package com.upc.mobilitappv2.multimodal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.upc.mobilitappv2.sensors.SensorLoader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sqrt


class Multimodal(private val context: Context, private val sensorLoader: SensorLoader) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var fifoAct: LinkedList<String> = LinkedList<String>()


    private var locations = ArrayList<Location>()
    private var last_distance: Double = 0.0

    private var capturing = false

    fun initialize() {

        fifoAct= LinkedList<String>()
        locations = ArrayList<Location>()
        last_distance = 0.0

        val intent = Intent("multimodal")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.locations[locationResult.locations.size - 1]
                if (location != null) {
                    Log.d("LOCATION", location.latitude.toString())
                    locations.add(location)
                    var activity = sensorLoader.analyseLastWindow().toString()
                    if (activity!="-") {activity = activity+"last distance: "+last_distance.toString()}

                    if (activity.split(',')[0] == "OTHERS") {
                        if (isStill()) {
                            activity = "STILL,"+activity.split(',')[1]
                        }
                    }

                    fifoAct.add(activity.split(',')[0])
                    if (fifoAct.size > 3) {
                        fifoAct.removeFirst()
                    }
                    var fifoStr = ""
                    for (a in fifoAct){
                        if (fifoStr =="") {fifoStr = "$fifoStr$a "}
                        else {fifoStr = "$fifoStr,$a " }
                    }
                    intent.putExtra("fifo", fifoStr)
                    intent.putExtra("activity", activity)
                    intent.putExtra("location", location.latitude.toString()+","+location.longitude.toString())
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                }
            }
        }

        locationRequest = LocationRequest.create()
        locationRequest.interval = (20 * 1000).toLong() // 20 seconds
        locationRequest.fastestInterval = (18 * 1000).toLong() // 18 seconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

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

    fun isStill(): Boolean {
        if (locations.size >= 2){
            val loc0 = locations[locations.size-2]
            val loc1 = locations[locations.size-1]
            val dist = computeDistance(loc0.latitude, loc0.longitude, loc1.latitude, loc1.longitude)
            last_distance = dist
            Log.d("DIST", last_distance.toString())
            return dist < 20
        }
        return false
    }

    fun startCapture() {

        capturing = true

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

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        sensorLoader.initialize("Multimodal")
    }

    fun getLastLocation(): Location {
        Log.d("LOCATION", locations.size.toString())
        return locations.last()
    }

    fun getState(): Boolean {
        return capturing
    }

    fun stopCapture() {
        capturing = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorLoader.stopCapture()
    }


}