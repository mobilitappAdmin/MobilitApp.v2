package com.mobi.mobilitapp.screens.components

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.notifications.cancelReminders
import com.mobi.mobilitapp.notifications.setReminders
import com.mobi.mobilitapp.ui.theme.Orange
import java.time.format.DateTimeFormatter
import java.util.Calendar

fun <K, V> Map<K, V>.inverseMap() = map { Pair(it.value, it.key) }.toMap()

@Composable
fun selectableButtonList(sharedPreferences: SharedPreferences, options:List<String>,translationTable:Map<String,String>, prefName: String, title:String, selectedText: (String)-> Unit, extraText:List<String>? = listOf<String>()){
    val res = LocalContext.current
    var activity: String = sharedPreferences.getString(prefName, translationTable.keys.last())!!
    sharedPreferences.edit().putString(prefName, activity).apply();

    val selected = remember { mutableStateOf(options.indexOf(translationTable[activity])) }
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
                        activity = translationTable.inverseMap()[text]!!;
                        selected.value = index;
                        sharedPreferences.edit().putString(prefName, activity).apply();
                        selectedText(text)
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
                            activity = translationTable.inverseMap()[text]!!;
                            selected.value = index + 3
                            sharedPreferences.edit().putString(prefName, activity).apply();
                            selectedText(text)
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
            val t = extraText[selected.value].split("\n")
            Spacer(Modifier.height(5.dp))
            Text(t[0], textAlign = TextAlign.Justify, fontSize = 12.sp,)

            Spacer(Modifier.height(15.dp))
            Text(t[1], textAlign = TextAlign.Justify, fontSize = 10.sp,)
        }
    }
    Spacer(Modifier.height(15.dp))
}

@Composable
fun selectableButtonListReminders(sharedPreferences: SharedPreferences, options:List<String>,translationTable:Map<String,String>, prefName: String, title:String, selectedText: (String)-> Unit){
    val res = LocalContext.current
    var activity: String = sharedPreferences.getString(prefName, translationTable.keys.last())!!
    sharedPreferences.edit().putString(prefName, activity).apply();

    val selected = remember { mutableStateOf(options.indexOf(translationTable[activity])) }

    val baseRequestCode = 110
    val times = remember { getTimes(sharedPreferences).toMutableStateList()}
    val size_time = remember { mutableStateOf(times.size)}
    val calendars  = remember{ MutableList(times.size) {Calendar.getInstance()} }

    if(times.isEmpty()){
        calendars.add(Calendar.getInstance());
        times.add("07:00")
        size_time.value +=1
    }

    cancelReminders(res, baseRequestCode)
    if(selected.value == 0){ // daily
        times.forEachIndexed(){index, item->
            setReminders(res,"daily",item,baseRequestCode + index)
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
                .height(36.dp)){
            options.forEachIndexed { index, text ->
                Button(
                    onClick = {
                        activity = translationTable.inverseMap()[text]!!;
                        selected.value = index;
                        sharedPreferences.edit().putString(prefName, activity).apply();
                        selectedText(text)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)){
                calendars.forEachIndexed{index, item->
                    sharedPreferences.edit().putString("reminderTimer$index", times[index]).apply()
                    OutlinedButton(
                        onClick = {

                            val mHour = calendars[index][Calendar.HOUR_OF_DAY]
                            val mMinute = calendars[index][Calendar.MINUTE]
                            val mTimePickerDialog = TimePickerDialog(
                                    res,
                            {_, mHour : Int, mMinute: Int ->
                                times[index] = (if(mHour < 10 )"0" else "") +"$mHour:" +(if( mMinute <10 )"0" else "") + "$mMinute"
                            }, mHour, mMinute, true
                            )
                            mTimePickerDialog.show()
                                  },
                        border = BorderStroke(2.dp, Orange),
                        shape = CircleShape,
                        modifier = Modifier
                            .height(30.dp)

                        ,
                        contentPadding = PaddingValues(start = 10.dp,end = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange,backgroundColor = Color.Transparent),



                    ) {
                        Text(times[index],Modifier.padding(top = 2.dp,start = 5.dp, end = 5.dp).wrapContentHeight(align = Alignment.CenterVertically), fontSize = 14.sp)
                        if(size_time.value > 1){
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "remove timer",
                                modifier = Modifier.size(18.dp).
                                clickable {
                                    calendars.removeAt(index)
                                    times.removeAt(index)
                                    size_time.value -= 1
                                    sharedPreferences.edit().putString("reminderTimer${times.size}", "").apply() }
                            )
                        }

                    }
                }

                //add button
                if(size_time.value < 3){
                    OutlinedButton(
                        onClick = {
                            calendars.add(Calendar.getInstance());
                            times.add("${7 * (size_time.value+1)}:00")
                            size_time.value +=1
                        },
                        border = BorderStroke(2.dp, Orange),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(30.dp)
                            ,
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange, backgroundColor = Color.Transparent)


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
    Spacer(Modifier.height(5.dp))

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

fun removeTimes(sharedPreferences: SharedPreferences){
    Log.d("removing","removing")
    for(i in (0..2)){
        sharedPreferences.edit().putString("reminderTimer$i","").apply()

    }
}


