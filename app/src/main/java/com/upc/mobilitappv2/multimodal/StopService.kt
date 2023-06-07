package com.upc.mobilitappv2.multimodal

import android.location.Location
import android.telephony.SignalThresholdInfo
import android.util.Log
import java.util.*
import kotlin.math.cos
import kotlin.math.sqrt

class StopService(alpha: Double, max_radium: Int, num_points: Int, covering_threshold: Float = 75.0F) {

    val alpha = alpha
    val max_radium = max_radium
    val num_points = num_points
    var center = DoubleArray(2)
    val threshold = covering_threshold

    lateinit var fifoLocations: LinkedList<Location>

    fun initialize(){
        fifoLocations = LinkedList<Location>()
        center = doubleArrayOf(-1.0, -1.0)
    }

    fun addLocation(loc: Location): Pair<Float, Boolean> {
        fifoLocations.add(loc)
        if (fifoLocations.size > num_points) {
            fifoLocations.removeFirst()
        }
        center = calculateCenter(loc)

        return checkStop() to (checkStop() >= threshold)
    }

    private fun checkStop(): Float{
        if (fifoLocations.size < num_points){
            return 0.0F
        }
        else {
            val covered = coveredLocations(max_radium.toDouble())
            return ((covered/fifoLocations.size)*100).toFloat()
        }

    }

    private fun calculateCenter(new_location: Location): DoubleArray {
        var ewmaLat = center[0]
        var ewmaLon = center[1]

        if (ewmaLat == -1.0 && ewmaLon == -1.0) {
            return doubleArrayOf(new_location.latitude, new_location.longitude)
        }

        ewmaLat = alpha * new_location.latitude + (1 - alpha) * ewmaLat
        ewmaLon = alpha * new_location.longitude + (1 - alpha) * ewmaLon

        return doubleArrayOf(ewmaLat, ewmaLon)
    }

    private fun computeDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371.0
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val deltaLat = lat1Rad - lat2Rad
        val deltaLon = lon1Rad - lon2Rad
        val avgLat = (lat1Rad + lat2Rad) / 2

        val x = deltaLon * cos(avgLat)

        val dist = radius * sqrt(x * x + deltaLat * deltaLat) * 1000

        Log.d("STOP", " dist = $dist")

        return dist
    }

    private fun coveredLocations(radius: Double): Float {
        var insiders = 0
        val numLocations = fifoLocations.size

        Log.d("STOP", "(${center[0]}, ${center[1]})")

        for (i in 0 until numLocations) {
            val loc = fifoLocations[i]
            val distance = computeDistance(center[0], center[1], loc.latitude, loc.longitude)
            if (distance <= radius) {
                insiders++
            }
        }

        Log.d("STOP", "Num. insiders = $insiders")

        return insiders.toFloat()
    }
}