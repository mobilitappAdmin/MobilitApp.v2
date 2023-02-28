package com.upc.mobilitappv2.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class SensorLoader(private val context: Context): Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var sensorAcc: Sensor? = null
    private var sensorMag: Sensor? = null
    private var sensorGyr: Sensor? = null

    private var accArray: MutableList<FloatArray> = ArrayList()
    private var gyrArray: MutableList<FloatArray> = ArrayList()
    private var magArray: MutableList<FloatArray> = ArrayList()


    fun initialize(): Boolean {

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

            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            // on below line we are passing
            // proximity sensor event listener
            this,

            // on below line we are
            // setting sensors.
            sensorMag,

            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            // on below line we are passing
            // proximity sensor event listener
            this,

            // on below line we are
            // setting sensors.
            sensorGyr,

            SensorManager.SENSOR_DELAY_NORMAL
        )
        Log.d("SENSOR", "init sensor")
        return sensorAcc != null //return false if not available sensor
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

        if (event != null && event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val acc_x = event.values[0]
            val acc_y = event.values[1]
            val acc_z = event.values[2]
            accData= floatArrayOf(acc_x, acc_y, acc_z)

            accArray.add(accData)

            val output = "Acc -> x: $acc_x, y: $acc_y, z: $acc_z  ->  $currentDT"
            Log.d("SENSOR", output)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val gyr_x = event.values[0]
            val gyr_y = event.values[1]
            val gyr_z = event.values[2]
            gyrData= floatArrayOf(gyr_x, gyr_y, gyr_z)

            gyrArray.add(gyrData)

            val output = "Gyr -> x: $gyr_x, y: $gyr_y, z: $gyr_z  ->  $currentDT"
            Log.d("SENSOR", output)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val mag_x = event.values[0]
            val mag_y = event.values[1]
            val mag_z = event.values[2]
            magData= floatArrayOf(mag_x, mag_y, mag_z)

            magArray.add(magData)

            val output = "Mag -> x: $mag_x, y: $mag_y, z: $mag_z  ->  $currentDT"
            Log.d("SENSOR", output)
        }
    }

    fun stopCapture(): Array<MutableList<FloatArray>> {
        sensorManager.unregisterListener(this, sensorAcc)
        sensorManager.unregisterListener(this, sensorGyr)
        sensorManager.unregisterListener(this, sensorMag)
        Log.d("SENSOR", "Capture finished")

        return arrayOf(accArray, gyrArray, magArray)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}