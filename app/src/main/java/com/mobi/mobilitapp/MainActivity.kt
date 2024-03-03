package com.mobi.mobilitapp

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobi.mobilitapp.map.Mapa
import com.mobi.mobilitapp.multimodal.Multimodal
import com.mobi.mobilitapp.screens.MainScreen
import com.mobi.mobilitapp.screens.components.alertDialogBattery
import com.mobi.mobilitapp.screens.components.alertDialogReminder
import com.mobi.mobilitapp.sensors.SensorLoader
import com.mobi.mobilitapp.ui.theme.MobilitAppv2Theme


/**
 * Main activity of the application.
 */
class MainActivity : ComponentActivity() {
    private lateinit var android_id: String
    private lateinit var sensorLoader: SensorLoader
    private lateinit var sensorLoaderMulti: SensorLoader
    private lateinit var multiModal: Multimodal
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mapa:Mapa

    /**
     * Initializes the activity and sets up the UI.
     *
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        createNotificationChannels()

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
        multiModal = Multimodal(this, sensorLoaderMulti, sharedPreferences, android_id)
        mapa = Mapa(this,sharedPreferences)

        setContent {
            val systemUiController = rememberSystemUiController()
            MobilitAppv2Theme {
                encryptedSharedPrefs(sharedPreferences, this)
                if(isSystemInDarkTheme()) systemUiController.setStatusBarColor(MaterialTheme.colors.background)
                else systemUiController.setStatusBarColor(MaterialTheme.colors.primary)
                // A surface container using the 'background' color from the theme
                Surface(color=MaterialTheme.colors.background) {
                    MainScreen(this, sensorLoader, multiModal, sharedPreferences,mapa)
                }
            }
        }

    }

    /**
     * Requests necessary permissions for the application.
     */
    private fun requestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.POST_NOTIFICATIONS
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
    private fun createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        // for HIGH priority notifications, with sound/vibration
        var name = getString(R.string.channel_nameHIGH)
        var descriptionText = getString(R.string.channel_descriptionHIGH)
        var importance = NotificationManager.IMPORTANCE_HIGH
        var channel = NotificationChannel(getString(R.string.channel_idHIGH), name, importance).apply {
            description = descriptionText
        }
        channel.setSound(null,null)
        // Register the channel with the system
        var notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


        // for LOW priority notifications, silent
        name = getString(R.string.channel_nameLOW)
        descriptionText = getString(R.string.channel_descriptionLOW)
        importance = NotificationManager.IMPORTANCE_LOW
        channel = NotificationChannel(getString(R.string.channel_idLOW), name, importance).apply {
            description = descriptionText
        }
        channel.setSound(null,null)
        // Register the channel with the system
        notificationManager=
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


    }



    /**
     * Creates a composable function for the encrypted shared preferences dialog.
     *
     * @param sharedPreferences The shared preferences instance.
     */
    @Composable
    private fun encryptedSharedPrefs(sharedPreferences: SharedPreferences, context: Context) {
        var openDialog0: Boolean by remember { mutableStateOf(!sharedPreferences.contains("location")) }
        var openDialog1: Boolean by remember { mutableStateOf(!sharedPreferences.contains("age")) }
        val res = LocalContext.current
        if (openDialog0) {
            var title: String = res.getString(R.string.LocationUse) +":"
            AlertDialog(
                onDismissRequest = {(context as? Activity)?.finish()},
                title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                },
                confirmButton = {
                    Button(onClick = {
                        // on below line we are storing data in shared preferences file.
                        sharedPreferences.edit().putBoolean("location", true).apply()
                        sharedPreferences.edit().commit()
                        openDialog0 = false
                    }) {
                        Text(res.getString(R.string.Accept))//res.getString(R.string.Accept)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            (context as? Activity)?.finish()
                        }) {
                        Text(res.getString(R.string.Deny))
                    }
                },
                text = { Text(res.getString(R.string.LocationPolicy))}
            )
        }
        if (!openDialog0 and openDialog1){
                var title = res.getString(R.string.Age2)
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
                            Text(res.getString(R.string.Next))
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
            var title: String = res.getString(R.string.Gender) +":"
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
                        Text(res.getString(R.string.Next))
                    }
                },
                text = {
                    var radioOptions = listOf(res.getString(R.string.man), res.getString(R.string.woman), res.getString(R.string.others), "NA")
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
        var openReminder: Boolean by rememberSaveable { mutableStateOf(!sharedPreferences.contains("reminder")) }
        if (!openDialog2 and openReminder) {
            alertDialogReminder(sharedPreferences = sharedPreferences, ongoing = {openReminder=it}, newText = {})
        }
        var openBattery: Boolean by rememberSaveable { mutableStateOf(!sharedPreferences.contains("battery")) }
        if (!openReminder and openBattery) {
            alertDialogBattery(sharedPreferences = sharedPreferences, ongoing = {openBattery=it}, newText = {})
        }
        if (!sharedPreferences.contains("debug")){
            sharedPreferences.edit().putBoolean("debug", false).apply()
            sharedPreferences.edit().commit()
        }
        //if (!sharedPreferences.contains("heuristic_fact")){
            sharedPreferences.edit().putFloat("heuristic_fact", 2.5f).apply()
            sharedPreferences.edit().putFloat("alpha", 0.2f).apply()
            sharedPreferences.edit().commit()
        //}
    }
}
