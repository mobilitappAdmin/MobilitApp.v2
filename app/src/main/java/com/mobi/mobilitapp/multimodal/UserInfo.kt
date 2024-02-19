package com.mobi.mobilitapp.multimodal

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Class representing user information and responsible for writing user data to a CSV file.
 *
 * @author Adrian Catalin and Jaume Planas
 * @property dir The directory path where the CSV file will be stored.
 * @property filename The name of the CSV file.
 */
class UserInfo(val dir: String, val filename: String) {

    var outputStream: FileWriter? = null
    var bw: BufferedWriter? = null
    var folder: File? = null
    var file: File? = null

    /**
     * Returns the size of the user data file in bytes.
     *
     * @return The size of the user data file.
     */
    fun getSize(): Long {
        return file!!.length()
    }

    /**
     * Opens the user data file for writing.
     *
     * @return `true` if the file was successfully opened, `false` otherwise.
     */
    fun open(): Boolean {
        folder = File(dir)
        if (!folder!!.exists()) {
            folder!!.mkdirs()
        }
        file = File(dir, filename)
        try {
            file!!.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            outputStream = FileWriter(file, true)
            bw = BufferedWriter(outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * Writes a line of data to the user data file.
     *
     * @param line The line of data to be written.
     * @return `true` if the line was successfully written, `false` otherwise.
     */
    fun writeLine(line: String): Boolean {
        try {
            bw!!.write(line + "\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * Closes the user data file.
     *
     * @return `true` if the file was successfully closed, `false` otherwise.
     */
    fun close(): Boolean {
        try {
            bw!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * Creates a new user data file and writes the user information to it.
     *
     * @param captureHash The capture hash value.
     * @param gender The user's gender.
     * @param ageRange The user's age range.
     * @param activityType The type of activity.
     * @param startLoc The starting location coordinates [latitude, longitude].
     * @param endLoc The ending location coordinates [latitude, longitude].
     * @param startTime The start time of the activity.
     * @param endTime The end time of the activity.
     * @return `true` if the user information was successfully written to the file, `false` otherwise.
     */
    fun createUserInfoDataFile(
        captureHash: Int,
        gender: String,
        ageRange: String,
        activityType: String,
        startLoc: Array<String>,
        endLoc: Array<String>,
        startTime: String,
        endTime: String
    ): Boolean {
        try {
            open()
            writeLine(
                captureHash.toString() + "," + gender + "," + ageRange + ","
                        + startTime + "," + startLoc[1] + "," + startLoc[0] + ","
                        + endTime + "," + endLoc[1] + "," + endLoc[0] + "," + activityType
            )
            close()
            Log.v("USER-INFO", "User info successfully written in csv file")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("USER-INFO", e.toString())
            return false
        }
    }


}