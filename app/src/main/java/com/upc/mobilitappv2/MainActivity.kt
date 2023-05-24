package com.upc.mobilitappv2

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.MainScreen
import com.upc.mobilitappv2.screens.components.PreferencesDialog
import com.upc.mobilitappv2.sensors.SensorLoader
import com.upc.mobilitappv2.ui.theme.MobilitAppv2Theme


class MainActivity : ComponentActivity() {
    private lateinit var android_id: String
    private lateinit var sensorLoader: SensorLoader
    private lateinit var sensorLoaderMulti: SensorLoader
    private lateinit var multiModal: Multimodal
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        // creating a master key for encryption of shared preferences.
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // Initialize/open an instance of EncryptedSharedPreferences on below line.
        sharedPreferences = EncryptedSharedPreferences.create(
            // passing a file name to share a preferences
            "preferences",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        sensorLoader = SensorLoader(this, android_id)
        sensorLoaderMulti = SensorLoader(this, android_id)
        multiModal = Multimodal(this, sensorLoaderMulti, sharedPreferences)
        Log.d("ID", android_id)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(MaterialTheme.colors.primary)
            MobilitAppv2Theme {
                encryptedSharedPrefs(this, sharedPreferences)
                // A surface container using the 'background' color from the theme
                Surface(color=MaterialTheme.colors.background) {
                    MainScreen(this, sensorLoader, multiModal, sharedPreferences)
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
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
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

    @Composable
    private fun encryptedSharedPrefs(context: Context, sharedPreferences: SharedPreferences) {
        // on below line creating a variable for message.

        var openDialog1: Boolean by remember { mutableStateOf(!sharedPreferences.contains("age")) }
        if (openDialog1){
                var title = "Age:"
                var activity: String? = null
                AlertDialog(
                    onDismissRequest = {openDialog1 = false},
                    title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                    },
                    confirmButton = {
                        Button(onClick = {
                            // on below line we are storing data in shared preferences file.
                            sharedPreferences.edit().putString("age", activity).apply()
                            sharedPreferences.edit().commit()
                            openDialog1 = false
                        }) {
                            Text("Next")
                        }
                    },
                    text = {
                        var radioOptions = listOf("1-17", "18-29", "30-44", "45-59", "60-79", "80+", "NA")
                        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[radioOptions.size -1]) }
                        activity=selectedOption
                        Column(
                            modifier= Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = { onOptionSelected(text) }
                                        )
                                        .padding(horizontal = 16.dp)
                                ){
                                    RadioButton(
                                        selected = (text == selectedOption),
                                        modifier = Modifier.padding(all = Dp(value = 2F)),
                                        onClick = {
                                            onOptionSelected(text)
                                            activity = text
                                        })
                                    Text(
                                        fontSize = 18.sp,
                                        text = text,
                                        modifier = Modifier.padding(start = 2.dp, top=16.dp)
                                    )

                                }
                            }
                        }
                    }
                )
            }
        var openDialog2: Boolean by remember { mutableStateOf(!sharedPreferences.contains("gender")) }
        if (!openDialog1 and openDialog2){
            var title: String = "Gender:"
            var activity: String? = null
            AlertDialog(
                onDismissRequest = {openDialog2 = false},
                title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                },
                confirmButton = {
                    Button(onClick = {
                        // on below line we are storing data in shared preferences file.
                        sharedPreferences.edit().putString("gender", activity).apply()
                        sharedPreferences.edit().commit()
                        openDialog2 = false
                    }) {
                        Text("Next")
                    }
                },
                text = {
                    var radioOptions = listOf("men", "women", "others", "NA")
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[radioOptions.size -1]) }
                    activity=selectedOption
                    Column(
                        modifier= Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        radioOptions.forEach { text ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) }
                                    )
                                    .padding(horizontal = 16.dp)
                            ){
                                RadioButton(
                                    selected = (text == selectedOption),
                                    modifier = Modifier.padding(all = Dp(value = 2F)),
                                    onClick = {
                                        onOptionSelected(text)
                                        activity = text
                                    })
                                Text(
                                    fontSize = 18.sp,
                                    text = text,
                                    modifier = Modifier.padding(start = 2.dp, top=16.dp)
                                )

                            }
                        }
                    }
                }
            )
        }
        if (!sharedPreferences.contains("debug")){
            sharedPreferences.edit().putBoolean("debug", false).apply()
            sharedPreferences.edit().commit()
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