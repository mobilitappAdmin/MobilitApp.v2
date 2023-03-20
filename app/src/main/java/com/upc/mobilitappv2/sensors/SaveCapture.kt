package com.upc.mobilitappv2.sensors

import android.util.Log
import java.io.*

class SaveCapture(fileStoreDir: String, filename: String) {

    private var dir = fileStoreDir
    private var filename = filename

    var outputStream: FileWriter? = null
    var bw: BufferedWriter? = null

    lateinit var folder: File
    lateinit var file: File

    fun getSize(): Long {
        return file.length()
    }

    fun open(): Boolean {
        folder = File(dir)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        file = File(dir, filename)
        try {
            file.createNewFile()
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
            bw!!.write(
                """
                $line
                
                """.trimIndent()
            )
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
}