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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.upc.mobilitappv2.screens.components.TopBar
import com.upc.mobilitappv2.ui.theme.Orange

/**
 * Composable function for displaying the preferences screen.
 *
 * @param context The context of the application.
 * @param preferences The SharedPreferences instance used for storing preferences.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreferencesScreen(context: Context, preferences: SharedPreferences) {
    Scaffold( topBar = { TopBar("Preferences") }) {
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
    val uri = "http://mobilitat.upc.edu/#"
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
            Text(text = gender.toString())
        }
        Divider()
        /*
        SwitchSetting(
            title = "Debug Mode",
            description = "Enable debug mode",
            checked = debug == true,
            preferences
        )
        */

        Text(
            text = "About",
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
                    text = "Access to the project website",
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
                    text = "Pollution",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Download pollution documentation",
                    style = MaterialTheme.typography.body2
                )
            }
            Icon(
                Icons.Filled.Info,
                contentDescription = "pollution",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Divider()
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
                var radioOptions = listOf("man", "woman", "others", "NA")
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
    if(checkedState.value){
        Divider(Modifier.padding(bottom= 10.dp))
        TextBox(preferences)
    }
    Divider()
}

@Composable
fun TextBox(preferences: SharedPreferences) {
    Row(modifier = Modifier.fillMaxWidth().height(82.dp).padding(horizontal = 16.dp)){
        Column(
            modifier = Modifier.weight(4f)
        ) {
            Text(
                text = "Heuristic factor",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "Strength of route following\nDefault: 2.2",
                style = MaterialTheme.typography.body2
            )
        }
        var num by remember { mutableStateOf(preferences.getFloat("heuristic_fact",0f).toString()) }
        val focusManager = LocalFocusManager.current
        TextField(
            value = num,
            modifier = Modifier.weight(1f),
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



