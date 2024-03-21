package com.mobi.mobilitapp.screens.components

import androidx.compose.foundation.layout.fillMaxWidth
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobi.mobilitapp.ui.theme.TextOnBlack
import com.mobi.mobilitapp.ui.theme.TextOnWhite

@Composable
fun EmailTextField(email: String, onEmailChange: (String) -> Unit, disabled:Boolean = false) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            ,
        value = if(disabled) "" else email,
        enabled = !disabled,
        textStyle = if (!disabled) TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if(isSystemInDarkTheme()) TextOnBlack else  TextOnWhite) else TextStyle(),
        onValueChange = { onEmailChange(it) },
        label = { Text("UPC Email",style = TextStyle(fontSize = 14.sp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
    )
}
fun isValidEmail(email: String): Boolean {
    val emailRegex = "[a-zA-Z0-9._-]+@([a-zA-Z0-9.-]+\\.)?upc\\.edu".toRegex()
    return email.matches(emailRegex)
}
