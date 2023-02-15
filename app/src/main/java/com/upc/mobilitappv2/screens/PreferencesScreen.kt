package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.upc.mobilitappv2.screens.components.TopBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreferencesScreen() {
    Scaffold( topBar = { TopBar("Preferences") }) {
        BodyContent()
    }
}

@Composable
private fun BodyContent() {
    Text("Preferences Screen")
}