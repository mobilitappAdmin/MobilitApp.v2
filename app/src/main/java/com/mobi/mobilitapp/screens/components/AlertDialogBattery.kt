package com.mobi.mobilitapp.screens.components

import android.content.SharedPreferences
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun alertDialogBattery(sharedPreferences: SharedPreferences,finished: (Boolean)-> Unit){

}
@Composable
fun bbuton(t: Boolean, onChange: (Boolean)-> Unit){
    Button(onClick = {
                     onChange(true)
    }) {
       Text(text = "click em :)")
    }
}