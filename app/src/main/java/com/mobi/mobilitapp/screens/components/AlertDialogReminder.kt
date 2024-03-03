package com.mobi.mobilitapp.screens.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.notifications.AlarmBroadcastReceiver
import com.mobi.mobilitapp.ui.theme.Orange
import java.util.Calendar

@Composable
fun alertDialogReminder(context:Context,sharedPreferences: SharedPreferences,ongoing: (Boolean)-> Unit){
    //ongoing is set to false once the user clicks the button or outside the alert
    val res = LocalContext.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var activity: String = ""

    // Declaring and initializing a calendar
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar[Calendar.HOUR_OF_DAY]
    val mMinute = mCalendar[Calendar.MINUTE]

    val mTime = remember { mutableStateOf("08:00") }
    var radioOptions = listOf(res.getString(R.string.Daily), res.getString(R.string.Weekly), res.getString(R.string.Never))
    AlertDialog(
        onDismissRequest = {ongoing(false)},
        confirmButton = {
            TextButton(onClick = {
                // on below line we are storing data in shared preferences file.
                sharedPreferences.edit().putString("reminder", activity).apply()
                sharedPreferences.edit().commit()
                if(activity != radioOptions[-1]){
                    setReminders(context,if(activity==radioOptions[0])"daily" else "weekly",mTime.value)
                }
                ongoing(false)
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
            )) {
                Text(text = res.getString(R.string.Next),color = Orange,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(modifier= Modifier
                /*.height((screenHeight * 0.70).dp)
                .fillMaxSize()*/
                /*.background(Color.Yellow)*/
                ){
                Text(text = LocalContext.current.getString(R.string.ReminderPopUp),style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),)

                Column(modifier = Modifier
                    /*.fillMaxSize()*/
                    /*.background(Color.Cyan),*/
                    /*verticalArrangement = Arrangement.Center,*/
                    )
                {
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[radioOptions.size -1]) }
                    activity=selectedOption
                    radioOptions.forEach{text->
                        Column(Modifier.padding(top = if (text == radioOptions[1])0.dp else 20.dp, bottom = if (text == radioOptions[0]) 0.dp else 20.dp)){
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                /*.background(Color.Magenta),*/
                                ){
                                RadioButton(
                                    selected = (text == selectedOption),
                                    /*modifier = Modifier.background(Color.Green),*/
                                    onClick = {
                                        onOptionSelected(text)
                                        activity = text
                                    })
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(top = 2.dp)
                                        .background(Color.White),
                                    fontSize = 18.sp,
                                    text = text,


                                    )
                            }
                            if(text == radioOptions[0] ){
                                // Fetching local context
                                val mContext = LocalContext.current

                                // Creating a TimePicker dialog
                                val mTimePickerDialog = TimePickerDialog(
                                    mContext,
                                    {_, mHour : Int, mMinute: Int ->
                                        mTime.value = (if(mHour < 10 )"0" else "") +"$mHour:" +(if( mMinute <10 )"0" else "") + "$mMinute"
                                    }, mHour, mMinute, true
                                )
                                Button(modifier= Modifier.height(40.dp).align(Alignment.CenterHorizontally),onClick = { mTimePickerDialog.show() },enabled = (selectedOption == text), colors = ButtonDefaults.buttonColors(backgroundColor = Orange)) {
                                    Text(text = mTime.value, color = Color.White, fontSize = 16.sp,)
                                }

                            }
                        }


                    }

                }
            }
        }
    )
}

fun setReminders(context: Context, frequency:String, time:String) {
    val intent = Intent(context, AlarmBroadcastReceiver::class.java)
    intent.putExtra("content",context.getString(R.string.ReminderNotification))
    val pendingIntent = PendingIntent.getBroadcast(context, 112, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    val calendar: Calendar = Calendar.getInstance()
    if(frequency == "daily"){
        var t = time.split(":")
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY,t[0].toInt())
        calendar.set(Calendar.MINUTE, t[1].toInt())
        calendar.set(Calendar.SECOND, 0)
    }
    else{//weekly
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_WEEK,1)
        calendar.set(Calendar.HOUR_OF_DAY, 16)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }
    Log.d("reminder set at", calendar.toString())
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent);
}

