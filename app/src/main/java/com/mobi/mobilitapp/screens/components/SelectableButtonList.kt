package com.mobi.mobilitapp.screens.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.notifications.AlarmBroadcastReceiver
import com.mobi.mobilitapp.ui.theme.LightOrange
import com.mobi.mobilitapp.ui.theme.LighterOrange
import com.mobi.mobilitapp.ui.theme.Orange
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun selectableButtonList(sharedPreferences: SharedPreferences, options:List<String>, prefName: String, title:String, selectedText: (String)-> Unit, extraText:List<String>? = listOf<String>()){
    val res = LocalContext.current
    var activity: String = sharedPreferences.getString(prefName, options.last())!!
    sharedPreferences.edit().putString(prefName, activity).apply();
    val selected = remember { mutableStateOf(options.indexOf(activity)) }
    Text(modifier = Modifier.fillMaxWidth(), text = title,style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
    Spacer(Modifier.height(10.dp))

    //adapt to higher number of options
    var list = if(options.size <5) options else options.slice(0..2)
    var blist = if(options.size <5) listOf<String>() else options.slice(3 until options.size)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)){
        Row(
            Modifier
                .fillMaxWidth()
                .height(36.dp)) {
            list.forEachIndexed { index, text ->
                Button(
                    onClick = {
                        activity = text;
                        selected.value = index;
                        sharedPreferences.edit().putString(prefName, activity).apply();
                        selectedText(activity)
                    },
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        top = 4.dp,
                        end = 4.dp,
                        bottom = 4.dp,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 1.dp, end = 1.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (selected.value == index) Orange else Color.Gray)
                ) {
                    Text(text, fontSize = 14.sp, color = Color.White)
                }
            }
        }
        if (blist.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)) {
                blist.forEachIndexed { index, text ->
                    Button(
                        onClick = {
                            activity = text;
                            selected.value = index + 3
                            sharedPreferences.edit().putString(prefName, activity).apply();
                            selectedText(activity)
                        },
                        contentPadding = PaddingValues(
                            start = 4.dp,
                            top = 4.dp,
                            end = 4.dp,
                            bottom = 4.dp,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 1.dp, end = 1.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = if (selected.value == index + 3) Orange else Color.Gray)
                    ) {
                        Text(text, fontSize = 14.sp, color = Color.White)
                    }
                }
            }

        }
    }
    if (extraText != null) {
        if(extraText.isNotEmpty()){
            Spacer(Modifier.height(5.dp))
            Text(extraText[selected.value])
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
fun selectableButtonListReminders(sharedPreferences: SharedPreferences, options:List<String>, prefName: String, title:String, selectedText: (String)-> Unit){
    val res = LocalContext.current
    var activity: String = sharedPreferences.getString(prefName, options.last())!!
    sharedPreferences.edit().putString(prefName, activity).apply();

    val baseRequestCode = 110
    val times = remember { getTimes(sharedPreferences).toMutableStateList()}
    val size = remember { mutableStateOf(times.size)}
    val calendars  = remember{ MutableList(times.size) {Calendar.getInstance()} }

    val selected = remember { mutableStateOf(options.indexOf(activity)) }

    cancelReminders(res, baseRequestCode)
    if(selected.value == 0){ // daily
        times.forEachIndexed(){index, item->
            setReminderos(res,"daily",item,baseRequestCode + index)
        }
    } else if(selected.value == 1){
            setReminders(res,"weekly","",baseRequestCode)
    }
    Text(modifier = Modifier.fillMaxWidth(), text = title,style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
    Spacer(Modifier.height(10.dp))
    Column(){
        Row(
            Modifier
                .fillMaxWidth()
                .height(36.dp)) {
            options.forEachIndexed { index, text ->
                Button(
                    onClick = {
                        activity = text;
                        selected.value = index;
                        sharedPreferences.edit().putString(prefName, activity).apply();
                        selectedText(activity)
                    },
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        top = 4.dp,
                        end = 4.dp,
                        bottom = 4.dp,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 1.dp, end = 1.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (selected.value == index) Orange else Color.Gray)
                ) {
                    Text(text, fontSize = 14.sp, color = Color.White)
                }
            }
        }
        //  calendars.add(Calendar.getInstance())
        Spacer(Modifier.height(10.dp))
        val formatter = DateTimeFormatter.ofPattern("ss")

        //if Daily, show time options
        if(selected.value == 0){
            //timer buttons
            Row(verticalAlignment = Alignment.CenterVertically){
                calendars.forEachIndexed{index, item->
                    sharedPreferences.edit().putString("reminderTimer$index", times[index]).apply()
                    OutlinedButton(
                        onClick = {
                            calendars.removeAt(index)
                            times.removeAt(index)
                            size.value -= 1
                            sharedPreferences.edit().putString("reminderTimer${times.size}", "").apply()
                                  },
                        border = BorderStroke(2.dp, LightOrange),
                        shape = CircleShape,
                        modifier = Modifier
                            .height(30.dp)
                            .weight(1f),
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange),



                    ) {
                        Text(times[index])
                    }
                }

                //add button
                if(size.value < 3){
                    OutlinedButton(
                        onClick = {
                            calendars.add(Calendar.getInstance());
                            times.add("08:00")
                            size.value +=1
                        },
                        border = BorderStroke(2.dp, LightOrange),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(30.dp)
                            .weight(1f),
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange)


                    ) {
                        // Inner content including an icon and a text label
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add timer",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            }

        }

    }
    Spacer(Modifier.height(10.dp))
}

fun getTimes(sharedPreferences: SharedPreferences):List<String>{
    var times:MutableList<String> = mutableListOf()
    for(i in (0..2)){
        var t = sharedPreferences.getString("reminderTimer$i","")
        Log.d("times.size","$i")
        if(t != ""){
            times.add(t!!)
        }
        else break
    }
    return times
}

fun setReminderos(context: Context, frequency:String, time:String, requestCode:Int) {
    val intent = Intent(context, AlarmBroadcastReceiver::class.java)
    intent.putExtra("content",context.getString(R.string.ReminderNotification))
    intent.putExtra("requestCode",requestCode)
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val alarmManager = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    val calendar: Calendar = Calendar.getInstance()
    if(frequency == "daily"){
        var t = time.split(":")
        calendar.set(Calendar.HOUR_OF_DAY,t[0].toInt())
        calendar.set(Calendar.MINUTE, t[1].toInt())
        calendar.set(Calendar.SECOND, 0)
    }
    else{//weekly
        calendar.set(Calendar.DAY_OF_WEEK,1)
        calendar.set(Calendar.HOUR_OF_DAY, 16)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent);
}

fun cancelReminders(context: Context, baseRequestCode:Int){
    for(i in (0..2)) {
        var requestCode = baseRequestCode + i
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("content", context.getString(R.string.ReminderNotification))
        intent.putExtra("requestCode", requestCode)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).cancel()
    }
}