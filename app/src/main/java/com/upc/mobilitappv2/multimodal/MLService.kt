package com.upc.mobilitappv2.multimodal

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
    * MLService is a class that provides machine learning functionality for multimodal transportation analysis.
    *
    * @property ctx The context used to access the application's assets.
    * @property model The TensorFlow Lite model interpreter used for inference.
    * @property NAMES A map that maps class labels to their corresponding names.
    * @property MODEL_FILE_NAME The name of the TensorFlow Lite model file.
    * @property NUM_STEPS The number of steps in the input data.
    * @property NUM_FEATURES The number of features in each step of the input data.
    * @property MEAN_TEST The mean values used for standardizing the input data.
    * @property STD_TEST The standard deviation values used for standardizing the input data.
 */
class MLService(val ctx: Context)
{
    private lateinit var model: Interpreter

    private val NAMES = mapOf(
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

    /**
     * Standarization values.
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

    /**
     * Initialize service by loading tflite interpreter.
     */
    fun initialize() {
        model = Interpreter(loadModelFile()!!, null)
    }

    /**
     * Load model in MappedByteBuffer format.
     */
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val assetFileDescriptor: AssetFileDescriptor = ctx.assets.openFd(MODEL_FILE_NAME)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val len = assetFileDescriptor.length

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, len)
    }

    /**
     * Standarize sample.
     *
     * @param data original sample
     * @return normalized sample
     */
    private fun standardizeData(data: FloatArray): FloatArray {
        val result = FloatArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i] - MEAN_TEST[i]) / STD_TEST[i]
        }
        return result
    }

    /**
     * Model inference of a single sample.
     *
     * @param sample float array of original sample.
     * @return activity prediction.
     */
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
        var maxval = 0.0f
        var maxIdx = -1
        for (i in 0 until output[0].size) {
            if (maxval < output[0][i]) {
                maxval = output[0][i]
                maxIdx = i
            }
        }

        return NAMES[maxIdx].toString()
    }

    /**
     * Model overall prediction.
     *
     * @param matrix of a number of samples.
     * @return overall prediction.
     */
    fun overallPrediction(matrix: Array<Array<FloatArray>>): String {
        var count = 0
        var predictions = mutableMapOf<String, Int>()
        NAMES.forEach { act ->
            predictions[act.value] = 0
        }
        matrix.forEach { sample ->
            val act = singleInference(sample).toString()
            predictions[act] = predictions[act]!! + 1
            ++count
        }
        val maxPred=predictions.maxWith(Comparator { x, y -> x.value.compareTo(y.value)})

        return maxPred.key
    }
}