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
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobi.mobilitapp.map.Mapa
import com.mobi.mobilitapp.multimodal.Multimodal
import com.mobi.mobilitapp.screens.MainScreen
import com.mobi.mobilitapp.screens.components.alertDialogEmail
import com.mobi.mobilitapp.screens.components.removeTimes
import com.mobi.mobilitapp.screens.components.selectableButtonList
import com.mobi.mobilitapp.screens.components.selectableButtonListReminders
import com.mobi.mobilitapp.sensors.SensorLoader
import com.mobi.mobilitapp.ui.theme.MobilitAppv2Theme
import com.mobi.mobilitapp.ui.theme.Orange
import com.mobi.mobilitapp.ui.theme.SoftGray


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


        //remove preferences the first time the app is launched, if needed
        if(!sharedPreferences.contains("flush")){
            sharedPreferences.edit().clear().commit()
            sharedPreferences.edit().putString("flush","flushed").commit()
        }




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
        var openLocation: Boolean by remember { mutableStateOf(!sharedPreferences.contains("location")) }
        var openPreferences: Boolean by rememberSaveable { mutableStateOf(!(sharedPreferences.contains("age") and
                                                                            sharedPreferences.contains("gender") and
                                                                            sharedPreferences.contains("battery")
                                                                            and sharedPreferences.contains("reminder")
                                                            )) }
//        var openReminder: Boolean by remember { mutableStateOf(!sharedPreferences.contains("reminder")) }
        val res = LocalContext.current
        if (openLocation) {
            var title: String = res.getString(R.string.LocationUse)
            var checked by remember { mutableStateOf(false)}
            AlertDialog(
                backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
                onDismissRequest = {(context as? Activity)?.finish()},
                title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // on below line we are storing data in shared preferences file.
                            sharedPreferences.edit().putBoolean("location", true).apply()
                            sharedPreferences.edit().commit()
                            openLocation = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Orange,
                            backgroundColor = Color.Transparent,
                            disabledBackgroundColor = Color.Transparent,
                            disabledContentColor = Color.Gray,

                        ),
                        enabled = checked)
                    {
                        Text(text = res.getString(R.string.Accept),style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            (context as? Activity)?.finish()
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                        )) {
                        Text(text = res.getString(R.string.Deny),color = Orange,style = TextStyle(fontSize = 16.sp))
                    }

                },
                text = {
                    Column(){
                        Text(res.getString(R.string.LocationPolicy), fontSize = 12.sp, textAlign = TextAlign.Justify)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                modifier = Modifier.scale(1f),
                                checked = checked,
                                onCheckedChange = { checked = it },
                                enabled = true,
                                colors = CheckboxDefaults.colors(checkedColor = Orange, uncheckedColor = Orange)
                            )
                            val uriHandler = LocalUriHandler.current
                            val annotatedString = buildAnnotatedString {
                                append(res.getString(R.string.CheckBoxPrivacyPolicy))
                                pushStringAnnotation(tag = "privacy_policy", annotation = res.getString(R.string.PrivacyPolicyLink))
                                withStyle(style = SpanStyle(color =Orange)) {
                                    append(res.getString(R.string.PrivacyPolicy))
                                }
                                pop()

                            }
                            Log.d("privact", annotatedString.toString())
                            ClickableText(text = annotatedString,style = TextStyle(fontSize = 14.sp, color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current), textAlign = TextAlign.Justify), onClick = { offset ->
                                annotatedString.getStringAnnotations(tag = "privacy_policy", start = offset, end = offset).firstOrNull()?.let {
                                    uriHandler.openUri(it.item)
                                }
                            })
                        }
                    }
                }


            )
        }

        if (!openLocation and openPreferences) {
            requestPermissions()
            AlertDialog(
                onDismissRequest = {/*openPreferences = false*/},
                backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
                confirmButton = {
                    TextButton(onClick = {

                        //remove stored timers if not needed, to refresh the list when entering the popup again
                        if(sharedPreferences.getString("reminder","") != "Daily")
                            removeTimes(sharedPreferences = sharedPreferences)

                        // on below line we are storing data in shared preferences file.
                        sharedPreferences.edit().commit()
                        openPreferences = false
                        mapa.triggerRecompose()
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent,)) {
                        Text(text = res.getString(R.string.Accept),color = Orange,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                },
                text = {
                    LazyColumn(Modifier.fillMaxWidth())
                    {
                        item() {
                            selectableButtonList(
                                sharedPreferences = sharedPreferences,
                                options = listOf(
                                    "1-17",
                                    "18-29",
                                    "30-44",
                                    "45-59",
                                    "60-79",
                                    "80+",
                                    "NA"
                                ),
                                translationTable = mapOf(
                                    "1-17" to "1-17",
                                    "18-29" to "18-29",
                                    "30-44" to "30-44",
                                    "45-59" to "45-59",
                                    "60-79" to "60-79",
                                    "80+" to "80+",
                                    "NA" to "NA"
                                ),
                                prefName = "age",
                                title = res.getString(R.string.Age),
                                selectedText = {},
                                extraText = null,
                            )
                            selectableButtonList(
                                sharedPreferences = sharedPreferences,
                                options = listOf(
                                    res.getString(R.string.man),
                                    res.getString(R.string.woman),
                                    res.getString(R.string.other),
                                    "NA"
                                ),
                                translationTable = mapOf(
                                    "Man" to res.getString(R.string.man),
                                    "Woman" to res.getString(R.string.woman),
                                    "Other" to res.getString(R.string.other),
                                    "NA" to "NA"
                                ),
                                prefName = "gender",
                                title = res.getString(R.string.Gender),
                                selectedText = {},
                                extraText = null
                            )
                            selectableButtonListReminders(
                                sharedPreferences = sharedPreferences,
                                options = listOf(
                                    res.getString(R.string.Daily),
                                    res.getString(R.string.Weekly),
                                    res.getString(R.string.Never)
                                ),
                                translationTable = mapOf(
                                    "Daily" to res.getString(R.string.Daily),
                                    "Weekly" to res.getString(R.string.Weekly),
                                    "Never" to res.getString(R.string.Never)
                                ),
                                prefName = "reminder",
                                title = res.getString(R.string.Reminders),
                                selectedText = {},
                            )
                            selectableButtonList(
                                sharedPreferences = sharedPreferences,
                                options = listOf(
                                    res.getString(R.string.Minimal),
                                    res.getString(R.string.Low),
                                    res.getString(R.string.Regular)
                                ),
                                translationTable = mapOf(
                                    "Minimal" to res.getString(R.string.Minimal),
                                    "Low" to res.getString(R.string.Low),
                                    "Regular" to res.getString(R.string.Regular)
                                ),
                                prefName = "battery",
                                title = res.getString(R.string.Battery),
                                selectedText = {},
                                extraText = listOf(
                                    res.getString(R.string.MinimalText) + "\n" + res.getString(R.string.batteryDisclaimer),
                                    res.getString(R.string.LowText) + "\n" + res.getString(R.string.batteryDisclaimer),
                                    res.getString(R.string.RegularText) + "\n" + res.getString(R.string.batteryDisclaimer)
                                )
                            )
                        }
                    }
                }
            )
        }
//        if (!openPreferences and openReminder) {
//            alertDialogReminder(sharedPreferences = sharedPreferences, ongoing = {openReminder = it}, newText = {})
//        }


        //DESCOMENTAR TOTS ELS BLOCS AMB EL TAG "SORTEIG" PER HABILITAR SORTEIG

        //SORTEIG
//        var mailSorteig: Boolean by remember { mutableStateOf(!sharedPreferences.contains("email")) }
//        if (!openPreferences and mailSorteig) { //email sorteig
//            alertDialogEmail(sharedPreferences = sharedPreferences, ongoing = {mailSorteig = it}, newText = {} )
//        }
//        if (!sharedPreferences.contains("draw")){
//            saveArray(arrayOf(), "draw", sharedPreferences)
//        }



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
