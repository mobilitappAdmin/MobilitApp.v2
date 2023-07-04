package com.upc.mobilitappv2.navigation

import com.upc.mobilitappv2.R

/**
 * Sealed class representing the different screens in the application.
 *
 * @author Gerard Caravaca
 * @param route The route associated with the screen.
 * @param title The title or name of the screen.
 * @param icon The icon resource ID for the screen.
 */
sealed class AppScreens(val route: String, val title: String, val icon: Int) {
    /**
     * Object representing the "Train Screen" in the application.
     */

    object TrainScreen: AppScreens("train_screen", "Generate data", R.drawable.baseline_save_24)
    /**
     * Object representing the "Predict Screen" in the application.
     */
    object PredictScreen: AppScreens("predict_screen", "Trip", R.drawable.baseline_pin_drop_24)
    /**
     * Object representing the "Preferences Screen" in the application.
     */
    object PreferencesScreen: AppScreens("preferences_screen", "Preferences", R.drawable.baseline_settings_24)
}
