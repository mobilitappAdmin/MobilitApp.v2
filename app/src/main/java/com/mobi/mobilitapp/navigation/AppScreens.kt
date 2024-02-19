package com.mobi.mobilitapp.navigation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mobi.mobilitapp.R

/**
 * Sealed class representing the different screens in the application.
 *
 * @author Gerard Caravaca
 * @param route The route associated with the screen.
 * @param title The title or name of the screen.
 * @param icon The icon resource ID for the screen.
 */

sealed class AppScreens(val route: String, val title: Int, val icon: Int) {
    /**
     * Object representing the "Train Screen" in the application.
     */


    object TrainScreen: AppScreens("train_screen",R.string.GenerateData, R.drawable.baseline_save_24)
    /**
     * Object representing the "Predict Screen" in the application.
     */
    object PredictScreen: AppScreens("predict_screen", R.string.Trip, R.drawable.baseline_pin_drop_24)
    /**
     * Object representing the "Preferences Screen" in the application.
     */
    object PreferencesScreen: AppScreens("preferences_screen", R.string.Preferences, R.drawable.baseline_settings_24)
}


