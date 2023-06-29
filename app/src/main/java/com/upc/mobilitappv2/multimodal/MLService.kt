package com.upc.mobilitappv2.multimodal

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MLService(val ctx: Context)
{
    private lateinit var model: Interpreter

    private val NAMES = mapOf(
        //0 to "Bicycle",
        0 to "Bus",
        1 to "Car",
        2 to "E-Scooter",
        3 to "Metro",
        4 to "Run",
        5 to "STILL",
        6 to "Train",
        7 to "Tram",
        8 to "WALK"
    )

    private val MODEL_FILE_NAME = "model_28juny.tflite"
    private val NUM_STEPS = 200
    private val NUM_FEATURES = 9

    /*
    private val MEAN_TEST = floatArrayOf(
        -0.026413381f, 0.07809378f, 0.3150015f, -4.897638f,
                -7.9428725f, -0.8115655f, -0.0033026477f, -0.0011234335f,
                -5.1337667E-4f
    )

    private val STD_TEST = floatArrayOf(
        7.5025744f, 5.943279f, 8.335319f, 1757.6592f,
        1994.8049f, 12915.904f, 0.2998825f, 0.28849712f,
        1.78979840e-01f,
    )
   */

    private val MEAN_TEST = floatArrayOf(
        -2.64133806e-02f,  7.80937799e-02f,  3.15001492e-01f, -4.89763796e+00f,
        -7.94287232e+00f, -8.11565515e-01f, -3.30264772e-03f, -1.12343347e-03f,
        -5.13376645e-04f
    )

    private val STD_TEST = floatArrayOf(
        2.73908276f,   2.43788407f,   2.88709522f,  41.92444589f,  44.66323892f,
        113.64816106f,   0.54761528f,   0.53711929f,   0.42306009f
    )

    fun initialize() {
        model = Interpreter(loadModelFile()!!, null)
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val assetFileDescriptor: AssetFileDescriptor = ctx.assets.openFd(MODEL_FILE_NAME)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val len = assetFileDescriptor.length

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, len)
    }

    private fun standardizeData(data: FloatArray): FloatArray {
        val result = FloatArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i] - MEAN_TEST[i]) / STD_TEST[i]
        }
        return result
    }

    fun singleInference(sample: Array<FloatArray>): String? {
        model.allocateTensors()
        val input = Array(1) {
            Array(NUM_STEPS) {
                FloatArray(NUM_FEATURES)
            }
        }
        for (i in 0 until NUM_STEPS){
            input[0][i] = standardizeData(sample[i])
        }
        val output =  Array(1) {
            FloatArray(NAMES.size)
        }

        model.run(input, output)
        val maxIdx =  output.indices.maxBy { output[0][it] } ?: -1
        //Log.d("ML", NAMES[maxIdx].toString())

        return NAMES[maxIdx].toString()
    }

    fun overallPrediction(matrix: Array<Array<FloatArray>>): String {
        var predictions = mutableMapOf<String, Int>()
        NAMES.forEach { act ->
            predictions[act.value] = 0
        }
        matrix.forEach { sample ->
            val act = singleInference(sample).toString()
            predictions[act] = predictions[act]!! + 1
        }
        val maxPred=predictions.maxWith(Comparator { x, y -> x.value.compareTo(y.value)})

        return maxPred.key
    }
}