package com.upc.mobilitappv2.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun MapDialog(map: MutableMap<String, Int>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ML activity prediction",
                style = MaterialTheme.typography.h5,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            map.forEach { (key, value) ->
                MinimalistMapItem(key, value)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.align(CenterHorizontally)
            ) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
fun MinimalistMapItem(key: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$key", style = MaterialTheme.typography.subtitle1, color = Color.White)
        Text(text = "$value", style = MaterialTheme.typography.body1, color = Color.White)
    }
}