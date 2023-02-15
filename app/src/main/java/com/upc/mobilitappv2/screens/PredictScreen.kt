package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.upc.mobilitappv2.screens.components.TopBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PredictScreen() {
    Scaffold(    topBar = { TopBar("Multimodal prediction") }) {
        BodyContent()
    }
}

@Composable
private fun BodyContent() {
    Text("Predict Screen")
}
