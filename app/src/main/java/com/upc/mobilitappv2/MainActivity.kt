package com.upc.mobilitappv2

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.upc.mobilitappv2.screens.MainScreen
import com.upc.mobilitappv2.sensors.SensorLoader
import com.upc.mobilitappv2.ui.theme.MobilitAppv2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilitAppv2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color=MaterialTheme.colors.background) {
                    MainScreen(this)
                }
            }
        }

    }
}


@Preview(showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    MobilitAppv2Theme {
        //MainScreen()
    }
}