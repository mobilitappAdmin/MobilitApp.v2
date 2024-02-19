package com.mobi.mobilitapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobi.mobilitapp.map.Mapa
import com.mobi.mobilitapp.multimodal.Multimodal
import com.mobi.mobilitapp.navigation.AppNavigation
import com.mobi.mobilitapp.navigation.AppScreens
import com.mobi.mobilitapp.sensors.SensorLoader

/**
 * MainScreen is the entry point of the application and displays the main user interface.
 *
 * @param context The context of the application.
 * @param sensorLoader The SensorLoader instance used for loading sensor data.
 * @param multiModal The Multimodal instance used for multimodal interaction.
 * @param sharedPreferences The SharedPreferences instance used for storing preferences.
 * @param mapa map instance used for displaying maps.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(context: Context, sensorLoader: SensorLoader, multiModal: Multimodal, sharedPreferences: SharedPreferences, mapa: Mapa) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        AppScreens.TrainScreen,
        AppScreens.PredictScreen,
        AppScreens.PreferencesScreen
    )

    Scaffold(bottomBar = { BottomNavigationBar(navController = navController, items = navigationItems) }) { innerPadding ->
        // Apply the padding globally to the whole BottomNavScreensController
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavigation(navController, context, sensorLoader, multiModal, sharedPreferences, mapa)
        }
    }
}

/**
 * Composable function for rendering the bottom navigation bar.
 *
 * @author Gerard Caravaca
 * @param navController The NavController instance for navigation between screens.
 * @param items The list of AppScreens representing the navigation items.
 */
@Composable
private fun BottomNavigationBar (navController: NavController, items: List<AppScreens>){

    val currentRoute = currentRoute(navController = navController)

    BottomNavigation {
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(painter=painterResource(id = screen.icon), contentDescription=screen.title) },
                label = { Text(screen.title, fontSize = 10.sp) },
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id){
                        saveState=true
                    }
                    launchSingleTop=true
                } },
                alwaysShowLabel = true
            )
        }
    }
}

/**
 * Retrieves the current route from the NavController.
 *
 * @param navController The NavController instance.
 * @return The current route as a String, or null if not available.
 */
@Composable
private fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}