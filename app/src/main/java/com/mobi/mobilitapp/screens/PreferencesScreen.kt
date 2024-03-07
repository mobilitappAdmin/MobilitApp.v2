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
import com.mobi.mobilitapp.getArray
import com.mobi.mobilitapp.screens.components.EmailTextField
import com.mobi.mobilitapp.screens.components.TopBar
import com.mobi.mobilitapp.screens.components.isValidEmail

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
    var debug: Boolean? by remember { mutableStateOf(preferences.getBoolean("debug", true)) }
    Log.d("PREFERENCES", "age: "+age+" gender: "+gender+" debug: "+debug.toString())

    var openDialog2: Boolean by remember { mutableStateOf(false) }
    var openDialog1: Boolean by remember { mutableStateOf(false) }
    var emailDialog: Boolean by remember { mutableStateOf(false) }

    val res = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = res.getString(R.string.Settings),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(10.dp)
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
                .clickable { openDialog2 = true }
        ){
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
        //APP PLAYSTORE
        /*
        SwitchSetting(
            title = res.getString(R.string.DebugMode),
            description = res.getString(R.string.pref3),
            checked = debug == true,
            preferences
        )
         */


        Text(
            text = res.getString(R.string.About),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(10.dp)
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
                .height(85.dp)
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
                    style = MaterialTheme.typography.caption
                )
            }
            Icon(
                Icons.Filled.Info,
                contentDescription = res.getString(R.string.Pollution),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Divider()
        Text(
            text = res.getString(R.string.draw),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(10.dp)
        )
        Divider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .clickable { emailDialog = true }
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = res.getString(R.string.email),
                    style = MaterialTheme.typography.subtitle1
                )
                preferences.getString("email", "")?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = res.getString(R.string.drawProgress),
                    style = MaterialTheme.typography.subtitle1
                )
                LinearProgressIndicator(
                    progress = getProgressRaffle(getArray("draw", preferences),
                        preferences.getString("email", "False").toString()
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }


    if (openDialog1){
        var title = res.getString(R.string.Age) +":"
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
                    Text(res.getString(R.string.Next))
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
        var title: String = res.getString(R.string.Gender) + ":"
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
                    Text(res.getString(R.string.Next))
                }
            },
            text = {
                var radioOptions = listOf(res.getString(R.string.man), res.getString(R.string.woman), res.getString(R.string.others), "NA")
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
    if (emailDialog) {
        var title: String = res.getString(R.string.email) +":"
        var email by remember { mutableStateOf("") }
        var valid by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {},
            title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
            },
            confirmButton = {
                Button(enabled = valid,
                    onClick = {
                        // on below line we are storing data in shared preferences file.
                        if (valid) {
                            preferences.edit().putString("email", email).apply()
                            preferences.edit().commit()
                        }
                        emailDialog = false
                    }) {
                    Text(res.getString(R.string.Accept))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        preferences.edit().putString("email", "False").apply()
                        preferences.edit().commit()
                        emailDialog = false
                    }) {
                    Text(res.getString(R.string.Deny))
                }
            },
            text = {
                val (email_, valid_) = ValidateEmail()
                email = email_
                valid = valid_
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

@Composable
fun ValidateEmail(): Pair<String, Boolean> {
    var email by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }

    Column (modifier = Modifier.padding(16.dp)) {
        Text(LocalContext.current.getString(R.string.emailInfo), style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.height(height = 20.dp))
        EmailTextField(email = email, onEmailChange = { email = it })

        if (email.isNotEmpty()) {
            if (isValidEmail(email)) {
                Text(text = LocalContext.current.getString(R.string.emailValid), color = Color.Blue)
                valid = true
            } else {
                Text(text = LocalContext.current.getString(R.string.emailNoValid), color = Color.Red)
                valid = false
            }
        }
    }

    return Pair(email, valid)
}

fun getProgressRaffle(array: Array<String?>?, email: String): Float {
    if (email == "False") {
        return 0f
    }
    if (array != null) {
        Log.d("PROGRESO size", array.size.toString())
    }
    if (array!!.size >= 3){
        return 1f
    }
    return array.size / 3f
}



