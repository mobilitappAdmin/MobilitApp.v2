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
    var title: String = LocalContext.current.getString(R.string.Sorteig)
    val res = LocalContext.current

    var organization by remember { mutableStateOf(sharedPreferences.getString("organization","")!!) }
    var role by remember { mutableStateOf(sharedPreferences.getString("role","")!!) }
    var grade by remember { mutableStateOf(sharedPreferences.getString("grade","")!!) }

    var email by remember {  mutableStateOf(sharedPreferences.getString("email","")!!) }
    var validEmail by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf((email != "") and (email != "False")) }

    var valid by remember { mutableStateOf(false) }
    valid = validEmail and                          //valid mail
            ( organization != "") and               //non void organization
            (role != "") and                        //non void role
            ((grade != "") or (role != "Student"));   //non void grade or grade disabled

    Log.d("email Outie",email)

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
                            sharedPreferences.edit().putString("organization", organization).apply()
                            sharedPreferences.edit().putString("role", role).apply()
                            if(role != "Student") //grade disabled
                                sharedPreferences.edit().putString("grade", "").apply()
                            else
                                sharedPreferences.edit().putString("grade", grade).apply()

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
                        checked = checked,
                        onCheckedChange = { checked = it },
                        enabled = true,
                        colors = CheckboxDefaults.colors(checkedColor = Orange, uncheckedColor = Orange)
                    )
                    Text(text = "I want to participate on the raffle",modifier = Modifier.padding(top = 1.dp), fontSize = 13.sp,textAlign = TextAlign.Justify)
                }

                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)){
                    dropDown(modifier = Modifier,value = organization,label="Organization",disabled = !checked,items = listOf("FIB - Facultad de Informática","ETSAB - Facultad de Arquitectura","ETSETB - Facultad de Telecomunicaciones","ETSECCB - Facultad de Ingenieria Civil"),selectedText = {organization = it})
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)){
                        dropDown(modifier = Modifier.weight(1f),value = role,label="Role",disabled = !checked,items = listOf("Student","PDI","PAS/PTGAS"),selectedText = {role = it})
                        dropDown(modifier = Modifier.weight(1f),value = grade,label="Grade",disabled = (!checked or (role != "Student")),items = listOf("1st","2nd","3rd","4th","Master Degree"),selectedText = {grade = it})

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
    Log.d("email Innie",email)
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


