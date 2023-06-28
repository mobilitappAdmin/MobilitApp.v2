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
        0 to "Bicycle",
        1 to "Bus",
        2 to "Car",
        3 to "E-Scooter",
        4 to "Metro",
        5 to "Run",
        6 to "Stationary",
        7 to "Train",
        8 to "Tram",
        9 to "Walk"
    )

    private val MODEL_FILE_NAME = "model_12juny.tflite"
    private val NUM_STEPS = 200
    private val NUM_FEATURES = 9

    private val MEAN_TEST = floatArrayOf(
        -0.019633127f, 0.087701574f, 0.35467845f, -4.2605853f,
                -7.905628f, 1.0647204f, -0.0019038585f, -5.15451100e-04f,
                -0.0019038585f
    )

    private val STD_TEST = floatArrayOf(
        5.7433925f, 4.68296240f, 7.472755f, 1372.8038f,
        1915.3158f, 13894.318f, 0.20449808f, 0.20266408f,
        0.20449808f
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