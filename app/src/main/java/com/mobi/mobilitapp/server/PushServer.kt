package com.mobi.mobilitapp.server

import android.util.Log
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Class responsible for uploading a file to the server.
 *
 * @property filePath The path of the file to upload.
 */
class PushServer(private var filePath: String) {
    private val TAG = "Upload2Server"
    private var serverCode = 0
    private var serverMsg: String? = null

    /**
     * Uploads the file to the server.
     *
     * @return The response code from the server.
     */
    fun call(): Int {
        var con: HttpURLConnection? = null
        var dos: DataOutputStream? = null
        val URLServer = "http://mobilitat.upc.edu/csv/data.php"
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        val buffer: ByteArray
        val maxBufferSize = 1024 * 1024
        var isAlive: Boolean

        try {
            val home = URL("http://mobilitat.upc.edu")
            val homeConn = home.openConnection()
            homeConn.connect()
            isAlive = true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "The server is down...")
            isAlive = false
        }
        if (isAlive) {
            try {
                val fis = FileInputStream(File(filePath))
                Log.v(TAG, "SERVER: $isAlive")
                val mURL = URL(URLServer)
                con = mURL.openConnection() as HttpURLConnection

                // Allows Inputs/Outputs over the connection. Do not use a cached copy
                con.doOutput = true
                con!!.doInput = true
                con.useCaches = false

                // Open an HTTP connection to the URL
                con.requestMethod = "POST"
                con.setRequestProperty("Connection", "Keep-Alive")
                con.setRequestProperty("ENCTYPE", "multipart/form-data")
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                con.setRequestProperty("uploaded_file", filePath)
                dos = DataOutputStream(con.outputStream)
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes(
                    "Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                            + filePath + "\"" + lineEnd
                )
                dos.writeBytes(lineEnd)

                // Create a buffer of maximum size
                bytesAvailable = fis.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                // Read the file and write it into form...
                bytesRead = fis.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize)
                    bytesAvailable = fis.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fis.read(buffer, 0, bufferSize)
                }

                // Send multipart form data necessary after file data
                dos.writeBytes(lineEnd) // \r\n
                dos.writeBytes(twoHyphens + boundary + lineEnd) // --*****\r\n

                // Responses from the server (code and msg)
                serverCode = con.responseCode
                serverMsg = con.responseMessage
                Log.d(TAG, "HTTP Response is: $serverMsg: $serverCode")
                if (serverCode != 200) {
                    Log.d(TAG, "SERVER RESPONSE CODE ERROR$serverCode")
                    serverCode = 0
                }

                // Close the streams
                fis.close()
                dos.flush()
                dos.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                serverCode = 0
                return serverCode
            }
            return serverCode
        } else {
            return 0
        }
    }

}

