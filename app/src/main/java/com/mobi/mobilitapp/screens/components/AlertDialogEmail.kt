package com.mobi.mobilitapp.screens.components

import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.screens.ValidateEmail
import com.mobi.mobilitapp.ui.theme.Orange
import com.mobi.mobilitapp.ui.theme.SoftGray

@Composable
fun alertDialogEmail(sharedPreferences: SharedPreferences,ongoing: (Boolean)-> Unit,newText: (String)-> Unit){
//ongoing is set to false once the user clicks the button or outside the alert
    var title: String = LocalContext.current.getString(R.string.Sorteig)
    val res = LocalContext.current
    var email by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }
    val participates = remember { mutableStateOf(false) }
    AlertDialog(
        backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
        onDismissRequest = {},
        confirmButton = {
            if(participates.value){
                TextButton(enabled = valid,
                    onClick = {
                        // on below line we are storing data in shared preferences file.
                        if (valid) {
                            sharedPreferences.edit().putString("email", email).apply()
                            sharedPreferences.edit().commit()
                            newText(email)
                        }
                        ongoing(false)
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent,
                        disabledContentColor = Color.Gray
                    )) {
                    Text(text = res.getString(R.string.Participar),color = if(valid)Orange else Color.Gray,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                }
            }
            else{
                TextButton(
                    onClick = {
                        sharedPreferences.edit().putString("email", "False").apply()
                        sharedPreferences.edit().commit()
                        ongoing(false)
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                    )) {
                    Text(text = res.getString(R.string.NoParticipar),color = Orange,style = TextStyle(fontSize = 16.sp,fontWeight = FontWeight.Bold))
                }
            }

        },
        text = {
            Column {
                //titol
                Text(modifier = Modifier.fillMaxWidth(), text = title,style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))

                //disclaimer amb la url del sorteig
                val uriHandler = LocalUriHandler.current
                val annotatedString = buildAnnotatedString {
                    append("We are organizing a raffle among UPC students and personnel. If you would like to participate, check the box and fill the form below. You can find more information ")

                    pushStringAnnotation(tag = "raffle", annotation = "https://mobilitapp.upc.edu/")
                    withStyle(style = SpanStyle(color =Orange)) {
                        append("here.")
                    }
                    pop()
                }
                ClickableText(text = annotatedString,style = TextStyle(fontSize = 12.sp,textAlign = TextAlign.Justify, color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)), onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "raffle", start = offset, end = offset).firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
                })

                //checkbox de participar al sorteig
                Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        modifier = Modifier.scale(1f),
                        checked = participates.value,
                        onCheckedChange = { participates.value = it },
                        enabled = true,
                        colors = CheckboxDefaults.colors(checkedColor = Orange, uncheckedColor = Orange)
                    )
                    Text(text = "I want to participate on the raffle",modifier = Modifier.padding(top = 1.dp), fontSize = 13.sp,textAlign = TextAlign.Justify)
                }

                val (email_, valid_) = ValidateEmail()
                email = email_
                valid = valid_
            }

        }
    )
}

@Composable
fun ValidateEmail(): Pair<String, Boolean> {
    val pref = LocalContext.current.getSharedPreferences("preferences",0)
    var email by remember { mutableStateOf(pref.getString("email","")) }
    var valid by remember { mutableStateOf(false) }

    Column (modifier = Modifier.padding(16.dp)) {
        Text(LocalContext.current.getString(R.string.emailInfo), style = MaterialTheme.typography.body2, textAlign = TextAlign.Justify)
        Spacer(modifier = Modifier.height(height = 20.dp))
        EmailTextField(email = email!!, onEmailChange = { email = it })

        if (email!!.isNotEmpty()) {
            if (isValidEmail(email!!)) {
                Text(text = LocalContext.current.getString(R.string.emailValid))
                valid = true
            } else {
                Text(text = LocalContext.current.getString(R.string.emailNoValid), color = Color.Red)
                valid = false
            }
        }
    }

    return Pair(email!!, valid)
}



