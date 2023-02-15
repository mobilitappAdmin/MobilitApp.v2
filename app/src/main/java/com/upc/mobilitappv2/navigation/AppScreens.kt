package com.upc.mobilitappv2.navigation

import com.upc.mobilitappv2.R

sealed class AppScreens(val route: String, val title: String, val icon: Int) {
    object TrainScreen: AppScreens("train_screen", "Train", R.drawable.model_training)
    object PredictScreen: AppScreens("predict_screen", "Predict", R.drawable.multimodal)
    object PreferencesScreen: AppScreens("preferences_screen", "Preferences", R.drawable.settings)
}
