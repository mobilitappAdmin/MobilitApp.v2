package com.upc.mobilitappv2.navigation

import com.upc.mobilitappv2.R

sealed class AppScreens(val route: String, val title: String, val icon: Int) {
    object TrainScreen: AppScreens("train_screen", "Collecting", R.drawable.baseline_save_24)
    object PredictScreen: AppScreens("predict_screen", "Trip", R.drawable.baseline_pin_drop_24)
    object PreferencesScreen: AppScreens("preferences_screen", "Preferences", R.drawable.baseline_settings_24)
}
