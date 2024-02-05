package com.upc.mobilitappv2.multimodal

import android.location.Location
import android.telephony.SignalThresholdInfo
import android.util.Log
import java.util.*
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Service for detecting stops based on location data.
 *
 * @author Gerard Caravaca, Adrian Catalin, Jaume Planas.
 *
 * @property alpha The weight factor for calculating the exponential moving average.
 * @property max_radium The maximum radius threshold for considering a location within a stop.
 * @property num_points The number of location points to consider for detecting a stop.
 * @property covering_threshold The covering threshold percentage for considering a stop.
 */
class StopService(alpha: Double, max_radium: Int, num_points: Int, covering_threshold: Float = 75.0F) {

    val alpha = alpha
    val max_radium = max_radium
    val num_points = num_points
    var center = DoubleArray(2)
    val threshold = covering_threshold

    lateinit var fifoLocations: LinkedList<Location>


    /**
     * Initializes the StopService by creating a new LinkedList for storing location points
     * and setting the initial center coordinates to (-1, -1).
     */
    fun initialize(){
        fifoLocations = LinkedList<Location>()
        center = doubleArrayOf(-1.0, -1.0)
    }

    /**
     * Adds a new location point to the service and checks if a stop is detected.
     *
     * @param loc The new location to add.
     * @return A pair consisting of the coverage percentage and a boolean indicating if a stop is detected.
     */
    fun addLocation(loc: Location): Pair<Float, Boolean> {
        fifoLocations.add(loc)
        if (fifoLocations.size > num_points) {
            fifoLocations.removeFirst()
        }
        center = calculateCenter(loc)

        Log.d("STOP", checkStop().toString())

        return checkStop() to (checkStop() >= threshold)
    }

    /**
     * Calculates the coverage percentage of the current location points within the maximum radius.
     *
     * @return The coverage percentage.
     */
    private fun checkStop(): Float{
        if (fifoLocations.size < num_points){
            return 0.0F
        }
        else {
            val covered = coveredLocations(max_radium.toDouble())
            return ((covered/fifoLocations.size)*100).toFloat()
        }

    }

    /**
     * Calculates the center coordinates using exponential moving average (EMA) based on the new location.
     *
     * @param new_location The new location point.
     * @return The updated center coordinates.
     */
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

    /**
     * Computes the distance between two sets of coordinates using the Haversine formula.
     *
     * @param lat1 The latitude of the first location.
     * @param lon1 The longitude of the first location.
     * @param lat2 The latitude of the second location.
     * @param lon2 The longitude of the second location.
     * @return The distance between the two locations in meters.
     */
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

        return dist
    }

    /**
     * Determines the number of location points within the specified radius from the center coordinates.
     *
     * @param radius The maximum radius threshold.
     * @return The number of location points within the radius.
     */
    private fun coveredLocations(radius: Double): Float {
        var insiders = 0
        val numLocations = fifoLocations.size

        for (i in 0 until numLocations) {
            val loc = fifoLocations[i]
            val distance = computeDistance(center[0], center[1], loc.latitude, loc.longitude)
            if (distance <= radius) {
                insiders++
            }
        }

        return insiders.toFloat()
    }
}