package com.upc.mobilitappv2.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.upc.mobilitappv2.screens.PredictScreen
import com.upc.mobilitappv2.screens.PreferencesScreen
import com.upc.mobilitappv2.screens.TrainScreen


@Composable
fun AppNavigation(navController: NavHostController, context: Context) {
    NavHost(navController = navController, startDestination = AppScreens.TrainScreen.route) {
        composable(route = AppScreens.TrainScreen.route) {
            TrainScreen(context)
        }
        composable(route = AppScreens.PredictScreen.route) {
            PredictScreen()
        }
        composable(route = AppScreens.PreferencesScreen.route) {
            PreferencesScreen()
        }
    }

}
