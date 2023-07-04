package com.upc.mobilitappv2.screens.components

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable function representing the PreferencesDialog.
 *
 * @param showDialog Indicates whether the dialog should be shown.
 * @param type The type of preference dialog (e.g., "age" or "gender").
 * @param dismissDialog The callback function to dismiss the dialog.
 * @param sharedPreferences The instance of SharedPreferences.
 */
@Composable
fun PreferencesDialog(
    showDialog: Boolean,
    type: String,
    dismissDialog: () -> Unit,
    sharedPreferences: SharedPreferences
){
    var openDialog: Boolean by remember { mutableStateOf(showDialog) }
    if (openDialog){
        var title: String
        if (type == "age") {
            title = "Age:"}
        else {
            title = "Gender:"}

        var activity: String? = null
        AlertDialog(
            onDismissRequest = {openDialog = false},
            title = { Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                   },
            confirmButton = {
                Button(onClick = {
                    // on below line we are storing data in shared preferences file.
                    if (type == "age") {
                        sharedPreferences.edit().putString("age", activity).apply()
                        sharedPreferences.edit().commit()
                    } else {
                        sharedPreferences.edit().putString("gender", activity).apply()
                        sharedPreferences.edit().commit()
                    }
                    dismissDialog()
                }) {
                    Text("Next")
                }
            },
            text = {
                var radioOptions = listOf("")
                if  (type == "age") {
                    radioOptions = listOf("1-17", "18-29", "30-44", "45-59", "60-79", "80+", "NA")
                } else {
                    radioOptions = listOf("man", "woman", "others", "NA")
                }
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
}
