package com.mobi.mobilitapp.screens.components

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.mobi.mobilitapp.ui.theme.Orange
import com.mobi.mobilitapp.ui.theme.SoftGray
import com.mobi.mobilitapp.ui.theme.TextOnBlack
import com.mobi.mobilitapp.ui.theme.TextOnWhite

@Composable
fun alertDialogEmail(sharedPreferences: SharedPreferences,ongoing: (Boolean)-> Unit,newText: (String)-> Unit){
//ongoing is set to false once the user clicks the button or outside the alert
    var title: String = LocalContext.current.getString(R.string.Giveaway)
    val res = LocalContext.current

    //save preferences in english
    val transOrg = mapOf(
        "FIB" to res.getString(R.string.FIB),
        "ETSAB" to res.getString(R.string.ETSAB),
        "ETSETB" to res.getString(R.string.ETSETB),
        "ETSECCB" to res.getString(R.string.ETSECCB),
        "" to ""
    )
    var organization by remember { mutableStateOf(transOrg[sharedPreferences.getString("organization","")!!]!!) }

    val transRole = mapOf(
        "Student" to res.getString(R.string.Student),
        "PDI" to res.getString(R.string.PDI),
        "PAS" to res.getString(R.string.PAS),
        "" to ""
    )
    var role by remember { mutableStateOf(transRole[sharedPreferences.getString("role","")!!]!!) }

    val transGrade = mapOf(
        "1st" to res.getString(R.string._1st),
        "2nd" to res.getString(R.string._2nd),
        "3rd" to res.getString(R.string._3rd),
        "4th" to res.getString(R.string._4th),
        "Master" to res.getString(R.string._Master),
        "" to ""
    )
    var grade by remember { mutableStateOf(transGrade[sharedPreferences.getString("grade","")!!]!!) }



    var email by remember {  mutableStateOf(sharedPreferences.getString("email","")!!) }
    var validEmail by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf((email != "") and (email != "False")) }

    var valid by remember { mutableStateOf(false) }
    valid = validEmail and                          //valid mail
            ( organization != "") and               //non void organization
            (role != "") and                        //non void role
            ((grade != "") or (role != res.getString(R.string.Student)));   //non void grade or grade disabled


    AlertDialog(
        backgroundColor = if (!isSystemInDarkTheme()) Color.White else SoftGray,
        onDismissRequest = {},
        confirmButton = {
            if(checked){ // participate
                TextButton(enabled = valid,
                    onClick = {
                        // on below line we are storing data in shared preferences file.
                        if (valid) {
                            sharedPreferences.edit().putString("email", email).apply()
                            sharedPreferences.edit().putString("organization", transOrg.inverseMap()[organization]).apply()
                            sharedPreferences.edit().putString("role", transRole.inverseMap()[role]).apply()
                            if(role != res.getString(R.string.Student)) //grade disabled
                                sharedPreferences.edit().putString("grade", "").apply()
                            else
                                sharedPreferences.edit().putString("grade", transGrade.inverseMap()[grade]).apply()

                            sharedPreferences.edit().commit()
                            newText(email)
                        }
                        ongoing(false)
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent,
                        disabledContentColor = Color.Gray
                    )) {
                    Text(text = res.getString(R.string.Participate),color = if(valid)Orange else Color.Gray,style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                }
            }
            else{ // Dont participate
                TextButton(
                    onClick = {
                        sharedPreferences.edit().putString("email", "False").apply()
                        sharedPreferences.edit().putString("organization", "").apply()
                        sharedPreferences.edit().putString("role", "").apply()
                        sharedPreferences.edit().putString("grade", "").apply()
                        sharedPreferences.edit().commit()
                        newText("False")
                        ongoing(false)
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                    )) {
                    Text(text = res.getString(R.string.DontParticipate),color = Orange,style = TextStyle(fontSize = 16.sp,fontWeight = FontWeight.Bold))
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
                    append(res.getString(R.string.GiveawayExlpanation))

                    pushStringAnnotation(tag = "giveaway", annotation = "https://mobilitapp.upc.edu/")
                    withStyle(style = SpanStyle(color =Orange)) {
                        append(res.getString(R.string.here))
                    }
                    pop()
                }
                ClickableText(text = annotatedString,style = TextStyle(fontSize = 12.sp,textAlign = TextAlign.Justify, color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)), onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "giveaway", start = offset, end = offset).firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
                })

                //checkbox de participar al sorteig
                Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        modifier = Modifier.scale(1f),
                        checked = checked,
                        onCheckedChange = { checked = it },
                        enabled = true,
                        colors = CheckboxDefaults.colors(checkedColor = Orange, uncheckedColor = Orange)
                    )
                    Text(text = res.getString(R.string.GivewayCheck),modifier = Modifier.padding(top = 1.dp), fontSize = 13.sp,textAlign = TextAlign.Justify)
                }

                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)){
                    dropDown(modifier = Modifier,value = organization,label= res.getString(R.string.Organization),disabled = !checked,items = listOf(res.getString(R.string.FIB),res.getString(R.string.ETSAB),res.getString(R.string.ETSETB),res.getString(R.string.ETSECCB)),selectedText = {organization = it})
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)){
                        dropDown(modifier = Modifier.weight(1f),value = role,label=res.getString(R.string.Role),disabled = !checked,items = listOf(res.getString(R.string.Student),res.getString(R.string.PDI),res.getString(R.string.PAS)),selectedText = {role = it})
                        dropDown(modifier = Modifier.weight(1f),value = grade,label=res.getString(R.string.Grade),disabled = (!checked or (role != res.getString(R.string.Student))),items = listOf(res.getString(R.string._1st),res.getString(R.string._2nd),res.getString(R.string._3rd),res.getString(R.string._4th),res.getString(R.string._Master)),selectedText = {grade = it})

                    }
                    val (email_, valid_) = ValidateEmail(sharedPreferences, !checked)
                    email = email_
                    validEmail = valid_
                }




            }

        }
    )
}

@Composable
fun ValidateEmail(sharedPreferences: SharedPreferences,disabled:Boolean = false): Pair<String, Boolean> {
    val e = sharedPreferences.getString("email","")!!
    var email by remember { mutableStateOf( if (e != "False") e else "" )}
    var valid by remember { mutableStateOf(false) }

    Column (modifier = Modifier.fillMaxWidth()) {

        EmailTextField(email = email!!, onEmailChange = { email = it }, disabled = disabled)

        if (email!!.isNotEmpty() and !disabled) {
            if (isValidEmail(email!!)) {
                Text(text = LocalContext.current.getString(R.string.emailValid),Modifier.height(20.dp))
                valid = true

            } else {
                Text(text = LocalContext.current.getString(R.string.emailNoValid),Modifier.height(20.dp), color = Orange)
                valid = false
            }
        }
        else Spacer(Modifier.height(20.dp))

        Spacer(Modifier.height(5.dp))
        Text(LocalContext.current.getString(R.string.emailInfo), style = TextStyle(fontSize = 12.sp,textAlign = TextAlign.Justify) , textAlign = TextAlign.Justify, color = if(!disabled) Color.Unspecified else (if(isSystemInDarkTheme()) TextOnBlack.copy(alpha = ContentAlpha.medium) else  TextOnWhite.copy(alpha = ContentAlpha.medium)))
    }

    return Pair(email!!, valid)
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun dropDown(modifier:Modifier,label:String,value:String = "",items: List<String>, selectedText: (String)-> Unit,disabled:Boolean = false) {

    var expanded by remember { mutableStateOf(false) }
    var text by remember {mutableStateOf(value)}
    var width by remember { mutableStateOf(0.dp) }
    val source = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.width(IntrinsicSize.Max)
    ) {
        val localDensity = LocalDensity.current
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .noRippleClickable { if(!disabled) expanded = true }
                .onGloballyPositioned { coordinates ->
                    width = with(localDensity) { coordinates.size.width.toDp() }
                }
                ,
            textStyle = if (!disabled) TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if(isSystemInDarkTheme()) TextOnBlack else  TextOnWhite) else TextStyle(),
            value = if(disabled) "" else text,
            onValueChange = { selectedText(text) },
            readOnly = true,
            enabled = !disabled,
            interactionSource = source,
            label = { Text(text = label, style = TextStyle(fontSize = 14.sp)) },
            singleLine = true,



        )
        if (source.collectIsPressedAsState().value) expanded = true
        DropdownMenu(
            modifier = Modifier.width(width),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    text = s
                    selectedText(s)
                }) {
                    Text(text = s,style = TextStyle(fontSize = 14.sp))
                }
            }

        }
    }
}
inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier =
    this.then(
        composed {
            clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                onClick()
            }
        })


