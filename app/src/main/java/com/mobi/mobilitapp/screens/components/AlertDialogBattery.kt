package com.mobi.mobilitapp.screens.components

import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.ui.theme.Orange
import com.mobi.mobilitapp.ui.theme.SoftGray

@Composable
fun alertDialogBattery(sharedPreferences: SharedPreferences,ongoing: (Boolean)-> Unit,newText: (String)-> Unit){
//ongoing is set to false once the user clicks the button or outside the alert
    val res = LocalContext.current
    var activity: String = sharedPreferences.getString("battery", res.getString(R.string.Regular))!!
    var radioOptions = listOf(res.getString(R.string.Regular), res.getString(R.string.Low), res.getString(R.string.Minimal))
    var radioText = listOf(res.getString(R.string.RegularText), res.getString(R.string.LowText), res.getString(R.string.MinimalText))
    val selected = remember { mutableStateOf(radioOptions.indexOf(activity)) }
    AlertDialog(
        backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
        onDismissRequest = {ongoing(false)},
        confirmButton = {
            TextButton(onClick = {
                // on below line we are storing data in shared preferences file.
                sharedPreferences.edit().putString("battery", activity).apply()
                sharedPreferences.edit().commit()
                ongoing(false)
                newText(activity)
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
            )) {
                Text(text = res.getString(R.string.Next),color = Orange,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(Modifier.fillMaxWidth())
            {
                Text(text = "Battery",style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()){
                    radioOptions.forEachIndexed{index,text->
                        Button(
                            onClick={ activity = text;selected.value = index},
                            modifier = Modifier.weight(1f).padding(start= 1.dp, end = 1.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = if(selected.value == index) Orange else Color.Gray,)
                            ){
                            Text(text, fontSize = 14.sp,color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(radioText[selected.value])

            }
        }
    )
}

