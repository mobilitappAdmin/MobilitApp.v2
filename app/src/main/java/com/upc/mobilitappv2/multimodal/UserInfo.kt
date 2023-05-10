package com.upc.mobilitappv2.multimodal

import android.telephony.mbms.MbmsErrors.StreamingErrors
import android.util.Log
import java.io.*

class UserInfo(val dir: String, val filename: String) {

    var outputStream: FileWriter? = null
    var bw: BufferedWriter? = null
    var folder: File? = null
    var file: File? = null

    fun getSize(): Long {
        return file!!.length()
    }

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

    fun writeLine(line: String): Boolean {
        try {
            bw!!.write(line + "\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    fun close(): Boolean {
        try {
            bw!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

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