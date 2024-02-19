package com.mobi.mobilitapp.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mobi.mobilitapp.R

@Composable
fun MapDialog(map: MutableMap<String, Int>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = LocalContext.current.getString(R.string.predMap1),
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
                Text(text = LocalContext.current.getString(R.string.Close))
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