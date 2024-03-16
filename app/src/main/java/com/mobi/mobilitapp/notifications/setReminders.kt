package com.mobi.mobilitapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import com.mobi.mobilitapp.R
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

fun setReminders(context: Context, frequency:String, time:String, requestCode:Int) {
    val intent = Intent(context, AlarmBroadcastReceiver::class.java)
    intent.putExtra("content",context.getString(R.string.ReminderNotification))
    intent.putExtra("requestCode",requestCode)
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    val calendar = GregorianCalendar.getInstance()
    if(frequency == "daily"){
        var t = time.split(":")
//        if (calendar.get(Calendar.HOUR_OF_DAY) >= t[0].toInt()) {
//            calendar.add(Calendar.DAY_OF_MONTH, 1)
//        }
        calendar.set(Calendar.HOUR_OF_DAY, t[0].toInt())
        calendar.set(Calendar.MINUTE, t[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if(calendar.timeInMillis < System.currentTimeMillis()){
            calendar.add(Calendar.DAY_OF_MONTH,1)
        }
        Log.d("calendar Current Time", Date(System.currentTimeMillis()).toString())
        Log.d("calendar set at" , calendar.time.toString())

//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,  24 *60*60*1000, pendingIntent);

    }
    else{//weekly
        val hour = 9
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        if(calendar.timeInMillis < System.currentTimeMillis()){
            calendar.add(Calendar.DAY_OF_MONTH,7)
        }
        Log.d("calendar set at" , calendar.time.toString())
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,  7 * 24 *60*60*1000, pendingIntent);
    }

}


fun cancelReminders(context: Context, baseRequestCode:Int){
    Log.d("calendar cancelReminders","cancelRemidners")
    for(i in (0..2)) {
        var requestCode = baseRequestCode + i
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("content", context.getString(R.string.ReminderNotification))
        intent.putExtra("requestCode", requestCode)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent.cancel()
    }
}