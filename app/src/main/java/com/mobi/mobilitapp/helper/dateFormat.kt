package com.mobi.mobilitapp.helper

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun indrawDays(todayDate: Date): Boolean {
    val drawDays = arrayOf( "21", "22", "23", "24", "25", "26", "27")
    val drawMonth = "03"
    val drawYear = "2024"

    val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    val date = parser.parse(todayDate.toString())

    val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
    val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH) // MM for month as a number
    val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

    val currentDay = dayFormat.format(date)
    val currentMonth = monthFormat.format(date)
    val currentYear = yearFormat.format(date)

    Log.d("PROGRESO", "today $currentDay, $currentMonth, $currentYear")

    return currentYear == drawYear && currentMonth == drawMonth && drawDays.contains(currentDay)
}

fun formatDate(todayDate: Date): String {
    val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    val date = parser.parse(todayDate.toString())

    val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
    val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH) // MM for month as a number
    val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

    val currentDay = dayFormat.format(date)
    val currentMonth = monthFormat.format(date)
    val currentYear = yearFormat.format(date)

    return "$currentDay-$currentMonth-$currentYear"
}