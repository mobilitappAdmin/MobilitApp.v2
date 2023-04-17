package com.upc.mobilitappv2.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.upc.mobilitappv2.multimodal.Multimodal
import com.upc.mobilitappv2.navigation.AppNavigation
import com.upc.mobilitappv2.navigation.AppScreens
import com.upc.mobilitappv2.sensors.SensorLoader

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(context: Context, sensorLoader: SensorLoader, multiModal: Multimodal, sharedPreferences: SharedPreferences) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        AppScreens.TrainScreen,
        AppScreens.PredictScreen,
        AppScreens.PreferencesScreen
    )

    Scaffold(bottomBar = { BottomNavigationBar(navController = navController, items = navigationItems) }) {
        AppNavigation(navController, context, sensorLoader, multiModal, sharedPreferences)
    }
}

@Composable
private fun BottomNavigationBar (navController: NavController, items: List<AppScreens>){

    val currentRoute = currentRoute(navController = navController)

    BottomNavigation {
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(painter=painterResource(id = screen.icon), contentDescription=screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id){
                        saveState=true
                    }
                    launchSingleTop=true
                } },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    //MainScreen()
}