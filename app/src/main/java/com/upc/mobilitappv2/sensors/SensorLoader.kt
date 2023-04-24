package com.upc.mobilitappv2.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.upc.mobilitappv2.multimodal.FFT.Complex
import com.upc.mobilitappv2.multimodal.FFT.FFT
import com.upc.mobilitappv2.server.UploadService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.*

class SensorLoader(private val context: Context, android_id: String): Service(), SensorEventListener {

    private val FILE_STORE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/MobilitAppV2/sensors"

    private val ANDROID_ID = android_id

    private lateinit var sensorManager: SensorManager

    private var sensorAcc: Sensor? = null
    private var sensorMag: Sensor? = null
    private var sensorGyr: Sensor? = null

    private var accData: FloatArray = emptyArray<Float>().toFloatArray()
    private var gyrData: FloatArray = emptyArray<Float>().toFloatArray()
    private var magData: FloatArray = emptyArray<Float>().toFloatArray()

    private var accArray: MutableList<FloatArray> = ArrayList()
    private var gyrArray: MutableList<FloatArray> = ArrayList()
    private var magArray: MutableList<FloatArray> = ArrayList()

    private var fifoAcc: LinkedList<MutableList<FloatArray>> = LinkedList<MutableList<FloatArray>>()
    private var fifoMag: LinkedList<MutableList<FloatArray>> = LinkedList<MutableList<FloatArray>>()
    private var fifoGyr: LinkedList<MutableList<FloatArray>> = LinkedList<MutableList<FloatArray>>()
    private var currentAccWindow: MutableList<FloatArray> = ArrayList<FloatArray>()
    private var currentMagWindow: MutableList<FloatArray> = ArrayList<FloatArray>()
    private var currentGyrWindow: MutableList<FloatArray> = ArrayList<FloatArray>()
    private var currentDateWindow: MutableList<Date> = ArrayList()

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

        accData = emptyArray<Float>().toFloatArray()
        gyrData = emptyArray<Float>().toFloatArray()
        magData = emptyArray<Float>().toFloatArray()

        fifoAcc = LinkedList<MutableList<FloatArray>>()
        fifoMag = LinkedList<MutableList<FloatArray>>()
        fifoGyr = LinkedList<MutableList<FloatArray>>()
        currentAccWindow = ArrayList<FloatArray>()
        currentMagWindow = ArrayList<FloatArray>()
        currentGyrWindow = ArrayList<FloatArray>()
        currentDateWindow = ArrayList()

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
            accData = floatArrayOf(acc_x, acc_y, acc_z)

            //accArray.add(accData)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyr_x = event.values[0]
            gyr_y = event.values[1]
            gyr_z = event.values[2]
            gyrData = floatArrayOf(gyr_x, gyr_y, gyr_z)

            //gyrArray.add(gyrData)
        }
        if (event != null && event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_x = event.values[0]
            mag_y = event.values[1]
            mag_z = event.values[2]
            magData= floatArrayOf(mag_x, mag_y, mag_z)

            if ((gyrData.isNotEmpty()) and (accData.isNotEmpty())) {
                magArray.add(magData)
                accArray.add(accData)
                gyrArray.add(gyrData)

                if (activity == "Multimodal") {
                    currentAccWindow.add(accData)
                    currentMagWindow.add(magData)
                    currentGyrWindow.add(gyrData)
                    currentDateWindow.add(Calendar.getInstance().time)
                }

                val output = "Acc -> x: $acc_x, y: $acc_y, z: $acc_z  ->  $currentDT"
                val output3 = "Mag -> x: $mag_x, y: $mag_y, z: $mag_z  ->  $currentDT"
                val output2 = "Gyr -> x: $gyr_x, y: $gyr_y, z: $gyr_z  ->  $currentDT"

                Log.d("SENSOR", output)
                Log.d("SENSOR", output2)
                Log.d("SENSOR", output3)
            }

        }
    }

    fun analyseLastWindow(): String? {
        if ( currentAccWindow.size > 256) {
            var winSamples = currentAccWindow.size.toDouble()
            val winT =
                (currentDateWindow[currentDateWindow.size - 1].time - currentDateWindow[0].time) / 1000.0
            val samplingFrequency = floor(winSamples / winT)
            println(samplingFrequency)
            /* FFT */
            // Make the window size a power of two for the FFT algorithm
            val powerOfTwo = floor(ln(winSamples) / ln(2.0)).toInt()
            winSamples = 2.0.pow(powerOfTwo.toDouble())

            var accX: Array<Complex?> = arrayOfNulls<Complex>(winSamples.toInt())
            var accY: Array<Complex?> = arrayOfNulls<Complex>(winSamples.toInt())
            var accZ: Array<Complex?> = arrayOfNulls<Complex>(winSamples.toInt())

            val cplxFFTX: Array<Complex?>
            val cplxFFTY: Array<Complex?>
            val cplxFFTZ: Array<Complex?>

            val fftX = DoubleArray(winSamples.toInt())
            val fftY = DoubleArray(winSamples.toInt())
            val fftZ = DoubleArray(winSamples.toInt())

            // Separate the acc. components in three lists
            run {
                var i = 0
                while (i < winSamples) {
                    accX[i] = Complex(currentAccWindow[i][0].toDouble(), 0.0)
                    accY[i] = Complex(currentAccWindow[i][1].toDouble(), 0.0)
                    accZ[i] = Complex(currentAccWindow[i][2].toDouble(), 0.0)
                    i++
                }
            }

            // Perform the FFT and compute the magnitude of each component
            cplxFFTX = FFT.fft(accX)
            cplxFFTY = FFT.fft(accY)
            cplxFFTZ = FFT.fft(accZ)
            run {
                var i = 0
                while (i < winSamples) {
                    fftX[i] = cplxFFTX[i]!!.abs()
                    fftY[i] = cplxFFTY[i]!!.abs()
                    fftZ[i] = cplxFFTZ[i]!!.abs()
                    i++
                }
            }

            // Compute the PSD (Power Spectral Density) of each component
            val psdX = DoubleArray(winSamples.toInt() / 2)
            val psdY = DoubleArray(winSamples.toInt() / 2)
            val psdZ = DoubleArray(winSamples.toInt() / 2)
            val fAxis = DoubleArray(winSamples.toInt() / 2)
            var i = 0
            while (i < winSamples / 2) {
                psdX[i] = 10 * log10(
                    1 / (winSamples * samplingFrequency) * Math.pow(
                        fftX[i], 2.0
                    )
                )
                psdY[i] = 10 * log10(
                    1 / (winSamples * samplingFrequency) * Math.pow(
                        fftY[i], 2.0
                    )
                )
                psdZ[i] = 10 * log10(
                    1 / (winSamples * samplingFrequency) * Math.pow(
                        fftZ[i], 2.0
                    )
                )

                // We use this same loop to scale the frequency axis.
                fAxis[i] = i * (samplingFrequency / winSamples)
                i++
            }

            // Narrow the search in the range of 1.4 to 2.3 Hz
            val lowerIndex = abs(Arrays.binarySearch(fAxis, 1.4) + 1)
            val upperIndex = abs(Arrays.binarySearch(fAxis, 2.3) + 1)
            val psdXsubArray = psdX.copyOfRange(lowerIndex, upperIndex)
            val psdYsubArray = psdY.copyOfRange(lowerIndex, upperIndex)
            val psdZsubArray = psdZ.copyOfRange(lowerIndex, upperIndex)

            // If the maximum value of the PSD is greater than a given threshold (2dB)
            // the current window will be labeled as "Walk".
            Log.d("FFT", psdXsubArray.max().toString())
            val maxX: Int = psdX.indexOfFirst { it == psdXsubArray.max() }
            val maxY: Int = psdY.indexOfFirst { it == psdYsubArray.max() }
            val maxZ: Int = psdZ.indexOfFirst { it == psdZsubArray.max() }

            val auxAcc = currentAccWindow
            val auxMag = currentMagWindow
            val auxGyr = currentGyrWindow

            fifoAcc.add(auxAcc)
            fifoMag.add(auxMag)
            fifoGyr.add(auxGyr)

            if (fifoAcc.size > 3) {
                fifoAcc.removeFirst()
                fifoMag.removeFirst()
                fifoGyr.removeFirst()
            }
            currentAccWindow = ArrayList()
            currentMagWindow = ArrayList()
            currentGyrWindow = ArrayList()
            currentDateWindow.clear()
            if (psdX[maxX] > 2 || psdY[maxY] > 2 || psdZ[maxZ] > 2) {
                return ("""WALK
freqX: ${fAxis[maxX]} magX: ${psdX[maxX]}
freqY: ${fAxis[maxY]} magY: ${psdY[maxY]}
freqZ: ${fAxis[maxZ]} magZ: ${psdZ[maxZ]}""")
            } else {
                return ("""OTHERS
freqX: ${fAxis[maxX]} magX: ${psdX[maxX]}
freqY: ${fAxis[maxY]} magY: ${psdY[maxY]}
freqZ: ${fAxis[maxZ]} magZ: ${psdZ[maxZ]}""")
            }


            /* return String.valueOf(
                             fAxis[maxX]+" "+psdX[maxX]
                             +" "+fAxis[maxY]+" "+psdY[maxY]
                             +" "+fAxis[maxZ]+" "+psdZ[maxZ]
                     );
                     */
        } else {
            return "-"
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

        thread {
            Log.d("SENSOR", "Saving sensor data to files")

            val tm = context.getSystemService(TELEPHONY_SERVICE)

            // 3 SENSORS: ACC, MAG, GYR
            var filePart = 0
            var FILENAME_FORMAT =
                (ANDROID_ID
                        + "_" + activity
                        + "_%s" // sensor type
                        //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                        + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().time)
                        + "_" + String.format("%06d", filePart) + ".csv")

            var filename = String.format(FILENAME_FORMAT, "ACC-MAG-GYR")
            var csv = SaveCapture(FILE_STORE_DIR, filename)
            try {
                csv.open()
                //writeLine("sensor,timestamp,x,y,z");
                //for (SensorSample p : magneticFieldData) {
                val magSize: Int = magArray.size

                for (i in 0 until magSize) {
                    if (csv.getSize() > 300 * 1024) {
                        csv.close()
                        filePart++
                        FILENAME_FORMAT =  //tm.getDeviceId()
                            (ANDROID_ID
                                    + "_" + activity
                                    + "_%s" // sensor type
                                    //+ "_" + new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Calendar.getInstance().getTime())
                                    + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().time)
                                    + "_" + String.format("%06d", filePart) + ".csv")
                        filename = String.format(FILENAME_FORMAT, "ACC-MAG-GYR")
                        csv = SaveCapture(
                            FILE_STORE_DIR,
                            filename
                        )
                        csv.open()
                    } else {
                        csv.writeLine(
                            java.lang.Long.valueOf("99999999999") //timestamp
                                .toString() +
                                    "," + accArray[i][0].toString() + "," + accArray[i][1].toString() + "," + accArray[i][2].toString() +
                                    "," + magArray[i][0].toString() + "," + magArray[i][1].toString() + "," + magArray[i][2].toString() +
                                    "," + gyrArray[i][0].toString() + "," + gyrArray[i][1].toString() + "," + gyrArray[i][2].toString())
                    }
                }
                csv.close()
                uploaded=true
                //Toast.makeText(context, "Capture was saved succesfully", Toast.LENGTH_LONG).show()
                Log.d("SENSOR", "Capture was saved succesfully")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("BAYO CSV", e.toString())
            }
        }

        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("UP", "Uploading...")
        context.startService(intent)

        return true
    }

    fun deleteCapture(): Boolean {

        thread {
            Log.d("SENSOR", "Saving sensor data to files")

            val tm = context.getSystemService(TELEPHONY_SERVICE)
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

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}