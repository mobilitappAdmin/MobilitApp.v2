package com.mobi.mobilitapp.sensors

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Class responsible for saving captured sensor data to a file.
 *
 * @property fileStoreDir The directory where the file should be stored.
 * @property filename The name of the file.
 */
class SaveCapture(fileStoreDir: String, filename: String) {

    private var dir = fileStoreDir
    private var filename = filename

    var outputStream: FileWriter? = null
    var bw: BufferedWriter? = null

    lateinit var folder: File
    lateinit var file: File

    /**
     * Returns the size of the file in bytes.
     *
     * @return The size of the file.
     */
    fun getSize(): Long {
        return file.length()
    }

    /**
     * Opens the file for writing.
     *
     * @return True if the file was successfully opened, false otherwise.
     */
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

    /**
     * Writes a line of text to the file.
     *
     * @param line The line of text to write.
     * @return True if the line was successfully written, false otherwise.
     */
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

    /**
     * Closes the file.
     *
     * @return True if the file was successfully closed, false otherwise.
     */
    fun close(): Boolean {
        try {
            bw!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }
}