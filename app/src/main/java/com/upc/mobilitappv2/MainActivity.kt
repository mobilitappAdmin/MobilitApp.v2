package com.upc.mobilitappv2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.MainScreen
import com.upc.mobilitappv2.sensors.SensorLoader
import com.upc.mobilitappv2.server.UploadService
import com.upc.mobilitappv2.ui.theme.MobilitAppv2Theme
import java.security.AccessController.getContext


class MainActivity : ComponentActivity() {
    private lateinit var android_id: String
    private lateinit var sensorLoader: SensorLoader
    private lateinit var sensorLoaderMulti: SensorLoader
    private lateinit var multiModal: Multimodal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        sensorLoader = SensorLoader(this, android_id)
        sensorLoaderMulti = SensorLoader(this, android_id)
        multiModal = Multimodal(this, sensorLoaderMulti)
        Log.d("ID", android_id)
        setContent {
            MobilitAppv2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color=MaterialTheme.colors.background) {
                    MainScreen(this, sensorLoader, multiModal)
                }
            }
        }

    }

    private fun requestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("myz", "" + SDK_INT)
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 1
                ) //permission request code is just an int
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1
                )
            }
        }
    }
}


@Preview(showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    MobilitAppv2Theme {
        //MainScreen()
    }
}