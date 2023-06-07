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
        0 to "Bus",
        1 to "Car",
        2 to "E-Scooter",
        3 to "Metro",
        4 to "Run",
        5 to "Stationary",
        6 to "Train",
        7 to "Tram",
        8 to "Walk"
    )

    private val MODEL_FILE_NAME = "model.tflite"
    private val NUM_STEPS = 200
    private val NUM_FEATURES = 9

    private val MEAN_TEST = floatArrayOf(
        -0.0462006055f, 0.0276003838f, 0.33340199f, 0.0208084669f,
        -10.4566228f, 1.31600973f, -0.000123946232f, 0.000212353514f, -0.000123946232f
    )

    private val STD_TEST = floatArrayOf(
        1.94794261f, 1.57676541f, 2.48401428f, 9.84275273f,
        44.9785843f, 119.149548f, 0.67448543f, 0.6229364f, 0.67448543f
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