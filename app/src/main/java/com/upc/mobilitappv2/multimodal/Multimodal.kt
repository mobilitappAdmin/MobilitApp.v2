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


class Multimodal(private val context: Context, private val sensorLoader: SensorLoader) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locations = ArrayList<Location>()

    private var capturing = false

    fun initialize() {

        val intent = Intent("multimodal")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.locations[locationResult.locations.size - 1]
                if (location != null) {
                    Log.d("LOCATION", location.latitude.toString())
                    locations.add(location)
                    val activity = sensorLoader.analyseLastWindow().toString()
                    intent.putExtra("activity", activity)
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