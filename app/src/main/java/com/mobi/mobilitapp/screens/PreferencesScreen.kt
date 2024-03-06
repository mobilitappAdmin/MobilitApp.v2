package com.mobi.mobilitapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.screens.components.TopBar
import com.mobi.mobilitapp.screens.components.alertDialogReminder
import com.mobi.mobilitapp.screens.components.selectableButtonList
import com.mobi.mobilitapp.screens.components.selectableButtonListReminders
import com.mobi.mobilitapp.ui.theme.Orange

/**
 * Composable function for displaying the preferences screen.
 *
 * @param context The context of the application.
 * @param preferences The SharedPreferences instance used for storing preferences.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreferencesScreen(context: Context, preferences: SharedPreferences) {
    Scaffold( topBar = { TopBar(LocalContext.current.getString(R.string.Preferences)) }) {
        BodyContent(preferences)
    }
}



/**
 * Composable function for the body content of the preferences screen.
 *
 * @param preferences The SharedPreferences instance used for storing preferences.
 */
@Composable
private fun BodyContent(preferences: SharedPreferences) {
    val uriHandler = LocalUriHandler.current
    val uri = "https://mobilitapp.upc.edu"
    var age: String? by remember { mutableStateOf(preferences.getString("age", "")) }
    var gender: String? by remember { mutableStateOf(preferences.getString("gender", "")) }
    var reminder: String? by remember { mutableStateOf(preferences.getString("reminder", "")) }
    var battery: String? by remember { mutableStateOf(preferences.getString("battery", "")) }
    var debug: Boolean? by remember { mutableStateOf(preferences.getBoolean("debug", true)) }
    Log.d("PREFERENCES", "age: "+age+" gender: "+gender+" debug: "+debug.toString())

    var openPreferences: Boolean by remember { mutableStateOf(false) }
    var openReminder: Boolean by remember { mutableStateOf(false) }

    val res = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = res.getString(R.string.Settings),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(16.dp)
        )

        Divider()
        Column(Modifier.clickable{ openPreferences = true}){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)


            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = res.getString(R.string.Age),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = res.getString(R.string.pref1),
                        style = MaterialTheme.typography.body2
                    )
                }
                Text(text = age.toString())
            }
            Divider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)

            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = res.getString(R.string.Gender),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = res.getString(R.string.pref2),
                        style = MaterialTheme.typography.body2
                    )
                }
                Text(text = gender.toString())
            }
            Divider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = res.getString(R.string.Battery),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = res.getString(R.string.BatteryUsage),
                        style = MaterialTheme.typography.body2
                    )
                }
                Text(text = battery.toString())
            }

        }

        Divider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .clickable { openReminder = true }
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = res.getString(R.string.Reminders),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = res.getString(R.string.ReminderPreferences),
                    style = MaterialTheme.typography.body2
                )
            }
            Text(text = reminder.toString())
        }


        Divider()
        //APP PLAYSTORE

        SwitchSetting(
            title = res.getString(R.string.DebugMode),
            description = res.getString(R.string.pref3),
            checked = debug == true,
            preferences
        )


        Text(
            text = res.getString(R.string.About),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(16.dp)
        )
        Divider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .clickable { uriHandler.openUri(uri) }
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Mobilitat Info",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = res.getString(R.string.pref4),
                    style = MaterialTheme.typography.body2
                )
            }
            Icon(
                Icons.Filled.Search,
                contentDescription = "web",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Divider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .clickable { uriHandler.openUri("http://147.83.252.137:8080/pollution") }
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = res.getString(R.string.Pollution),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = res.getString(R.string.pref5),
                    style = MaterialTheme.typography.body2
                )
            }
            Icon(
                Icons.Filled.Info,
                contentDescription = res.getString(R.string.Pollution),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Divider()
    }

    if(openPreferences){
        val res = LocalContext.current
        AlertDialog(
            onDismissRequest = {/*openPreferences = false*/},
            confirmButton = {
                TextButton(onClick = {
                    // on below line we are storing data in shared preferences file.
                    preferences.edit().commit()
                    openPreferences = false
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                )) {
                    Text(text = res.getString(R.string.Accept),color = Orange,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(Modifier.fillMaxWidth())
                {
                    selectableButtonList(
                        sharedPreferences = preferences,
                        options = listOf("1-17", "18-29", "30-44", "45-59", "60-79", "80+", "NA"),
                        prefName = "age" ,
                        title = res.getString(R.string.Age),
                        selectedText = {age = it},
                        extraText = null,
                    )
                    selectableButtonList(
                        sharedPreferences = preferences,
                        options = listOf(res.getString(R.string.man), res.getString(R.string.woman), res.getString(R.string.other), "NA"),
                        prefName = "gender" ,
                        title = res.getString(R.string.Gender),
                        selectedText = {gender = it},
                        extraText = null
                    )
                    /*selectableButtonListReminders(
                        sharedPreferences = preferences,
                        options = listOf(res.getString(R.string.Daily), res.getString(R.string.Weekly), res.getString(R.string.Never)),
                        prefName = "reminder" ,
                        title = res.getString(R.string.Reminders),
                        selectedText = {reminder = it},
                    )*/
                    selectableButtonList(
                        sharedPreferences = preferences,
                        options = listOf(res.getString(R.string.Minimal), res.getString(R.string.Low),res.getString(R.string.Regular)),
                        prefName = "battery" ,
                        title = res.getString(R.string.Battery),
                        selectedText = {battery = it},
                        extraText = listOf(res.getString(R.string.MinimalText), res.getString(R.string.LowText),res.getString(R.string.RegularText))
                    )
                }
            }
        )
    }
    if(openReminder){
        alertDialogReminder(sharedPreferences = preferences, ongoing = {openReminder = it} , newText = {reminder = it} )
    }

}


/**
 * A composable function that displays a switch setting.
 *
 * @author Gerard Caravaca
 * @param title The title of the switch setting.
 * @param description The description of the switch setting.
 * @param checked The initial checked state of the switch.
 * @param preferences The SharedPreferences instance to store the switch state.
 */
@Composable
fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    preferences: SharedPreferences
) {
    val checkedState = remember { mutableStateOf(checked) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = description,
                style = MaterialTheme.typography.body2
            )
        }

        Switch(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                preferences.edit().putBoolean("debug", checkedState.value).apply()
                preferences.edit().commit()

            }
        )


    }
    /*
    if(checkedState.value){
        Divider(Modifier.padding(bottom= 10.dp))
        //TextBox(preferences) //heuristic factor
        TextBox_stop(preferences)
    }
     */
    Divider()
}

@Composable
fun TextBox(preferences: SharedPreferences) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(90.dp)
        .padding(horizontal = 16.dp)){
        Column(
            modifier = Modifier.weight(3f)
        ) {
            Text(
                text = LocalContext.current.getString(R.string.pref6),
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = LocalContext.current.getString(R.string.pref7),
                style = MaterialTheme.typography.body2
            )
        }
        var num by remember { mutableStateOf(preferences.getFloat("heuristic_fact",0f).toString()) }
        val focusManager = LocalFocusManager.current
        TextField(
            value = num,
            modifier = Modifier.weight(2f),
            colors = TextFieldDefaults.textFieldColors(
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            onValueChange = {
                if (it.isEmpty() || (it.toDoubleOrNull() != null && (it.length < 4) && it.toDouble() < 10)) {
                    num = it
                    }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done),
            keyboardActions= KeyboardActions(
                onDone = {
                    if(num.toDoubleOrNull() != null){
                        preferences.edit().putFloat("heuristic_fact",num.toFloat()).apply()
                        focusManager.clearFocus();
                    }

                },
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),

        )
    }


}

@Composable
fun TextBox_stop(preferences: SharedPreferences) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(90.dp)
        .padding(horizontal = 16.dp)){
        Column(
            modifier = Modifier.weight(3f)
        ) {
            Text(
                text = LocalContext.current.getString(R.string.pref8),
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = LocalContext.current.getString(R.string.pref9),
                style = MaterialTheme.typography.body2
            )
        }
        var num by remember { mutableStateOf(preferences.getFloat("alpha",0f).toString()) }
        val focusManager = LocalFocusManager.current
        TextField(
            value = num,
            modifier = Modifier.weight(2f),
            colors = TextFieldDefaults.textFieldColors(
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            onValueChange = {
                if (it.isEmpty() || (it.toDoubleOrNull() != null && (it.length < 6) && it.toDouble() < 100)) {
                    num = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done),
            keyboardActions= KeyboardActions(
                onDone = {
                    if(num.toDoubleOrNull() != null){
                        preferences.edit().putFloat("alpha",num.toFloat()).apply()
                        focusManager.clearFocus();
                    }

                },
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),

            )
    }


}



