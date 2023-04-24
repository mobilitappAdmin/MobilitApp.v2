package com.upc.mobilitappv2.navigation

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.screens.PredictScreen
import com.upc.mobilitappv2.screens.PreferencesScreen
import com.upc.mobilitappv2.screens.TrainScreen
import com.upc.mobilitappv2.sensors.SensorLoader


@Composable
fun AppNavigation(navController: NavHostController, context: Context, sensorLoader: SensorLoader, multiModal: Multimodal, sharedPreferences: SharedPreferences) {
    NavHost(navController = navController, startDestination = AppScreens.TrainScreen.route) {
        composable(route = AppScreens.TrainScreen.route) {
            TrainScreen(context, sensorLoader)
        }
        composable(route = AppScreens.PredictScreen.route) {
            PredictScreen(context, multiModal, sharedPreferences)
        }
        composable(route = AppScreens.PreferencesScreen.route) {
            PreferencesScreen(context, sharedPreferences)
        }
    }

}
