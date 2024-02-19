package com.mobi.mobilitapp.screens.components

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R


/**
 * Composable function representing the top app bar.
 *
 * @param namePage The name of the current page.
 */
@Composable
fun TopBar(namePage: String) {
    TopAppBar {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.high,
            LocalTextStyle provides MaterialTheme.typography.h6
        ) {
            if (namePage == LocalContext.current.getString(R.string.GenerateData)) {
                Text(
                    text = "MobilitApp: $namePage",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Text(
                    text = "MobilitApp: $namePage",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            val activity = (LocalContext.current as? Activity)
            Button(onClick = {
                activity?.finish()
            }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Exit")
            }
        }
    }
}