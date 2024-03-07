package com.mobi.mobilitapp

import android.content.SharedPreferences
import android.util.Log

fun saveArray(array: Array<String?>, arrayName: String, preferences: SharedPreferences): Boolean {
    var stringArray: String? = null
    for (date in array){
        if (date != null && date != "") {
            if (stringArray == null) {
                stringArray = date
            } else {
                stringArray += ",$date"
            }
        }
    }
    if (stringArray != null) {
        preferences.edit().putString(arrayName, stringArray).apply()
        preferences.edit().commit()
    }

    return true
}

fun getArray(arrayName: String, preferences: SharedPreferences): Array<String?> {
    val arrayString = preferences.getString(arrayName, null)
    if (arrayString != null) {
        Log.d("PROGRESO", arrayString)
        return arrayString.split(",").toTypedArray()
    }
    return arrayOf()
}

