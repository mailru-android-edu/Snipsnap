package com.wndenis.snipsnap.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

const val MIN_FILE_LENGTH = 5

const val HOUR_SEC = 3600L
const val DAY_SEC = HOUR_SEC * 24
const val WEEK_SEC = DAY_SEC * 7
const val MONTH_SEC = DAY_SEC * 30 // wndenis: rough approx
const val YEAR_SEC = DAY_SEC * 365

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
