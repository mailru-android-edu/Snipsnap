package com.wndenis.snipsnap

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val MIN_FILE_LENGTH = 5

fun LocalDateTime.conv(): String {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return this.format(df)
}

fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}

fun extractName(filename: String?): String {
    if (filename == null)
        return ""
    if (filename.length <= MIN_FILE_LENGTH)
        return ""
    return "${filename.subSequence(0, filename.length - 5)}"
}

fun makeName(filename: String): String {
    return "$filename.spsp"
}
