package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.upc.mobilitappv2.screens.components.TopBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TrainScreen() {
    Scaffold( topBar = { TopBar("Training") }) {
        BodyContent()
    }
}

@Composable
private fun BodyContent(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Train Screen")
        Button(onClick = { /*TODO*/ },
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = "Start")
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    TrainScreen()
}
