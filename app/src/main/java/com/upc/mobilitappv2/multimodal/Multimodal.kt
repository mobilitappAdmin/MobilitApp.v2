package com.upc.mobilitappv2.multimodal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.upc.mobilitappv2.sensors.SensorLoader
import java.io.IOException


class Multimodal(private val context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locations = ArrayList<Location>()

    fun initialize() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.locations[locationResult.locations.size - 1]
                if (location != null) {
                    Log.d("LOCATION", location.latitude.toString())
                    locations.add(location)
                    //wayLatitude.add(location.latitude)
                    //wayLongitude.add(location.longitude)
                    //locationAccuracy.add(location.accuracy)
                    //gpsTime =
                    //SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
                    //wayGPSTime.add(gpsTime)
                    /*
                    if (sensorLoader != null) {
                        try {
                            //updateUI(location, MainActivity.sensorLoger.analyseLastWindow())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            //updateUI(location, "-")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    */

                }
            }
        }

        locationRequest = LocationRequest.create()
        locationRequest.interval = (20 * 1000).toLong() // 20 seconds
        locationRequest.fastestInterval = (18 * 1000).toLong() // 18 seconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun startCapture() {
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
    }

    fun getLastLocation(): Location {
        Log.d("LOCATION", locations.size.toString())
        return locations.last()
    }

    fun stopCapture() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}