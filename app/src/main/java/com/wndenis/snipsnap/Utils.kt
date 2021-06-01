package com.wndenis.snipsnap

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.conv(): String {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    //getDateTimeInstance()//
    return this.format(df)
}

fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}