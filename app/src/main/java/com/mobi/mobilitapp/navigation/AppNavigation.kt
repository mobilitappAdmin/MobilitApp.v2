package com.mobi.mobilitapp.navigation

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobi.mobilitapp.map.Mapa
import com.mobi.mobilitapp.multimodal.Multimodal
import com.mobi.mobilitapp.screens.PredictScreen
import com.mobi.mobilitapp.screens.PreferencesScreen
import com.mobi.mobilitapp.screens.TrainScreen
import com.mobi.mobilitapp.sensors.SensorLoader


/**
 * Composable function that sets up the application's navigation using Jetpack Compose Navigation.
 *
 * @author Gerard Caravaca
 * @param navController The navigation controller used to navigate between different screens.
 * @param context The application context.
 * @param sensorLoader The sensor loader for accessing device sensors.
 * @param multiModal The multimodal instance used for multimodal predictions.
 * @param sharedPreferences The shared preferences instance for managing user preferences.
 * @param mapa The Map instance used for displaying the map screen.
 */
@Composable
fun AppNavigation(navController: NavHostController, context: Context, sensorLoader: SensorLoader, multiModal: Multimodal, sharedPreferences: SharedPreferences,mapa:Mapa) {
    NavHost(navController = navController, startDestination = AppScreens.TrainScreen.route) {
        composable(route = AppScreens.TrainScreen.route) {
            TrainScreen(sensorLoader)
        }
        composable(route = AppScreens.PredictScreen.route) {
            PredictScreen(context, multiModal, sharedPreferences,mapa)
        }
        composable(route = AppScreens.PreferencesScreen.route) {
            PreferencesScreen(context, sharedPreferences)
        }
    }

}
