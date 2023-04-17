package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upc.mobilitappv2.screens.components.TopBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreferencesScreen(context: Context, preferences: SharedPreferences) {
    Scaffold( topBar = { TopBar("Preferences") }) {
        BodyContent(preferences)
    }
}

@Composable
private fun BodyContent(preferences: SharedPreferences) {
    var age: String? by remember { mutableStateOf(preferences.getString("age", "")) }
    var gender: String? by remember { mutableStateOf(preferences.getString("gender", "")) }
    var debug: Boolean? by remember { mutableStateOf(preferences.getBoolean("debug", true)) }
    Log.d("PREFERENCES", "age: "+age+" gender: "+gender+" debug: "+debug.toString())

    var openDialog2: Boolean by remember { mutableStateOf(false) }
    var openDialog1: Boolean by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Settings",
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
                .clickable { openDialog1 = true }
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Age",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Select your age range",
                    style = MaterialTheme.typography.body2
                )
            }
            //Spacer(modifier = Modifier.height(height = 30.dp))
            Text(text = age.toString())
        }
        Divider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .clickable { openDialog2 = true }
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Select your gender",
                    style = MaterialTheme.typography.body2
                )
            }
            //Spacer(modifier = Modifier.height(height = 30.dp))
            Text(text = gender.toString())
        }
        Divider()

        SwitchSetting(
            title = "Debug Mode",
            description = "Enable debug mode",
            checked = debug == true,
            preferences
        )
    }

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
                    preferences.edit().putString("age", activity).apply()
                    preferences.edit().commit()
                    age = activity
                    openDialog1 = false
                }) {
                    Text("Next")
                }
            },
            text = {
                var radioOptions = listOf("1-17", "18-29", "30-44", "45-59", "60-79", "80+", "NA")
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[radioOptions.indexOf(age)]) }
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
                    preferences.edit().putString("gender", activity).apply()
                    preferences.edit().commit()
                    gender = activity
                    openDialog2 = false
                }) {
                    Text("Next")
                }
            },
            text = {
                var radioOptions = listOf("men", "women", "others", "NA")
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[radioOptions.indexOf(gender)]) }
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

}

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
    Divider()
}



