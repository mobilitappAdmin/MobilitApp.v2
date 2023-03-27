package com.upc.mobilitappv2.server

import android.app.IntentService
import android.content.Intent
import android.os.Environment
import android.util.Log
import java.io.File

class UploadService: IntentService("UploadService") {

    val FILEPATH = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath + "/MobilitAppV2/sensors"
    val TAG = "UploadService"
    var serverCode = 0

    override fun onHandleIntent(intent: Intent?) {
        // Gets data from the incoming Intent
        //val dataString = intent!!.getStringExtra("UP")

        // Do work here, based on the contents of dataString
        //File dir = new File(FILEPATH);
        //File[] files = dir.listFiles();
        Log.d(TAG, "UPLOADING...")

        Thread(
            Runnable {
                Log.d(TAG, "... ")
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                val dir = File(FILEPATH)
                val files = dir.listFiles()
                if (files != null) {
                    Log.d(TAG, "Number of files: " + files.size)
                }
                if (files != null) {
                    for (i in files.indices) {
                        if (files[i].isFile) {
                            val intentUpdate = Intent()
                            intentUpdate.action = "UPDATE"
                            intentUpdate.addCategory("DEFAULT")
                            intentUpdate.putExtra("KEY", i)
                            sendBroadcast(intentUpdate)
                            Log.d(
                                "NUMERO DE ITERACIÓN",
                                (i + 1).toString() + " of " + files.size.toString()
                            )
                            try {
                                val mFile = PushServer(files[i].toString())
                                serverCode = mFile.call()
                                Log.d(TAG, "Selected file " + files[i].toString())
                                if (serverCode != 200) {

                                    return@Runnable
                                } else {
                                    val deleteFile = File(files[i].toString())
                                    val delete = deleteFile.delete()
                                    if (delete) {
                                        Log.d(
                                            TAG, """The file ${files[i]} has been deleted
                                                        Server code: $serverCode"""
                                        )
                                        // Return Result
                                        val intentResponse = Intent()
                                        intentResponse.action = "RESPONSE"
                                        intentResponse.addCategory("DEFAULT")
                                        intentResponse.putExtra("OUT", "OK")
                                        sendBroadcast(intentResponse)
                                    }
                                }
                            } catch (ex: Exception) {
                                Log.d(TAG, "Upload fail")
                            }

                            /* Rok Slamek - for backup we don't erase files */
                            // When the Thread has finish we can erase the files and server response OK
                            if (serverCode == 200) {
                                val deleteFile = File(files[i].toString())
                                val delete = deleteFile.delete()
                                if (delete) {
                                    Log.d(
                                        TAG, """The file ${files[i]} has been deleted
            Server code: $serverCode"""
                                    )
                                    // Return Result
                                    val intentResponse = Intent()
                                    intentResponse.action = "RESPONSE"
                                    intentResponse.addCategory("DEFAULT")
                                    intentResponse.putExtra("OUT", "OK")
                                    sendBroadcast(intentResponse)
                                }
                            } else {
                                // Return Result
                                val intentResponse = Intent()
                                intentResponse.action = "RESPONSE"
                                intentResponse.addCategory("DEFAULT")
                                intentResponse.putExtra("OUT", "KO")
                                sendBroadcast(intentResponse)
                            }
                        }
                    }
                }
            }
        ).start()
    }

}