package com.upc.mobilitappv2.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.provider.Settings.Secure.ANDROID_ID
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class SensorLoader(private val context: Context): Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var sensorAcc: Sensor? = null
    private var sensorMag: Sensor? = null
    private var sensorGyr: Sensor? = null

    private var accArray: MutableList<FloatArray> = ArrayList()
    private var gyrArray: MutableList<FloatArray> = ArrayList()
    private var magArray: MutableList<FloatArray> = ArrayList()

    private var activity: String? = null
    var capturing = false
    var finishedCapture: Int? = null

    var startTime: String? = null
    var endTime: String? = null
    private val simpleDateFormat=SimpleDateFormat("yyyy-LL-dd HH:mm:ss")

    var uploaded = false
    fun getContext(): Context{
        return context
    }

    fun getUploadState(): Boolean {
        return uploaded
    }

    fun getTimes(): Array<String?> {
        return arrayOf(startTime, endTime)
    }

    fun initialize(selectedActivity: String): Boolean {

        activity=selectedActivity
        Toast.makeText(context,"selected activity: $activity", Toast.LENGTH_LONG).show()

        accArray=ArrayList()
        magArray=ArrayList()
        gyrArray=ArrayList()

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorGyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorManager.registerListener(
            // on below line we are passing
            // proximity sensor event listener
            this,

            // on below line we are
            // setting sensors.
            sensorAcc,

            10000
        )
        sensorManager.registerListener(
            // on below line we are passing
            // proximity sensor event listener
            this,

            // on below line we are
            // setting sensors.
            sensorMag,

            10000
        )
        sensorManager.registerListener(
            // on below line we are passing
            // proximity sensor event listener
            this,

            // on below line we are
            // setting sensors.
            sensorGyr,

            10000
        )
        Log.d("SENSOR", "init sensor")
        capturing = true
        startTime = simpleDateFormat.format(Date())
        endTime = null
        finishedCapture= null
        uploaded=false
        return sensorAcc != null //return false if not available sensor
    }

    fun getState(): Boolean {
        return capturing
    }

    fun getCapture(): Int? {
        return finishedCapture
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val simpleDateFormat= SimpleDateFormat("yyyy-LL-dd HH:mm:ss")
        val currentDT: String = simpleDateFormat.format(Date())

        var accData: FloatArray? =null
        var gyrData: FloatArray? =null
        var magData: FloatArray? =null

        var acc_x : kotlin.Float = 0.0F
        var acc_y : kotlin.Float = 0.0F
        var acc_z : kotlin.Float = 0.0F
        var gyr_x : kotlin.Float = 0.0F
        var gyr_y : kotlin.Float = 0.0F
        var gyr_z : kotlin.Float = 0.0F
        var mag_x : kotlin.Float = 0.0F
        var mag_y : kotlin.Float = 0.0F
        var mag_z : kotlin.Float = 0.0F


        if (event != null && event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            acc_x = event.values[0]
            acc_y = event.values[1]
            acc_z = event.values[2]
            accData= floatArrayOf(acc_x, acc_y, acc_z)

            accArray.add(accData)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyr_x = event.values[0]
            gyr_y = event.values[1]
            gyr_z = event.values[2]
            gyrData= floatArrayOf(gyr_x, gyr_y, gyr_z)

            gyrArray.add(gyrData)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_x = event.values[0]
            mag_y = event.values[1]
            mag_z = event.values[2]
            magData= floatArrayOf(mag_x, mag_y, mag_z)

            //sync sensors to magnetic freq
            magArray.add(magData)
            if (gyrData != null) {
                gyrArray.add(gyrData)
            }
            if (accData != null) {
                accArray.add(accData)
            }
            val output = "Acc -> x: $acc_x, y: $acc_y, z: $acc_z  ->  $currentDT"
            val output3 = "Mag -> x: $mag_x, y: $mag_y, z: $mag_z  ->  $currentDT"
            val output2 = "Gyr -> x: $gyr_x, y: $gyr_y, z: $gyr_z  ->  $currentDT"

            Log.d("SENSOR", output)
            Log.d("SENSOR", output2)
            Log.d("SENSOR", output3)
        }
    }

    fun stopCapture(): Int {
        sensorManager.unregisterListener(this, sensorAcc)
        sensorManager.unregisterListener(this, sensorGyr)
        sensorManager.unregisterListener(this, sensorMag)
        Log.d("SENSOR", "Capture finished")

        capturing = false
        finishedCapture = arrayOf(accArray, gyrArray, magArray)[0].size
        endTime = simpleDateFormat.format(Date())

        return arrayOf(accArray, gyrArray, magArray)[0].size
    }

    fun saveCapture(): Boolean {
        Thread.sleep(5_000)
        uploaded=true
        Toast.makeText(context, "Capture was uploaded succesfully", Toast.LENGTH_LONG).show()
        return true
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}