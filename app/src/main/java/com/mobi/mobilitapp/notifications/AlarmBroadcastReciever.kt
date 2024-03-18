package com.mobi.mobilitapp.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mobi.mobilitapp.MainActivity
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.getArray
import java.util.Date


class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("calendar Notification received", Date(System.currentTimeMillis()).toString())
        val array = getArray("draw", context.getSharedPreferences("preferences",0))
        var text = intent.getStringExtra("content")!!

        if(intent.hasExtra("reminder")){
            text = context.getString(R.string.ReminderNotification)
            if(array.isNotEmpty()){
                if(array.size<3) text = text + "\n" + context.getString(R.string.ReminderExtraTextConcurs,"${array.size}")
                else text = text + "\n" + context.getString(R.string.ReminderExtraTextConcursAcabat)
            }
        }

        showNotification(context, text,intent.getIntExtra("requestCode",0))
    }

    private fun showNotification(context: Context,text: String, requestCode: Int) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val t = text.split("\n")
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context,
                context.getString(R.string.channel_idHIGH)
            )
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(if(t.size>1) t[0] else text) // title for notification
                .setContentText(if(t.size>1) t[1] else null)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) { return }
            notify(1, mBuilder.build())
        }
    }
}
