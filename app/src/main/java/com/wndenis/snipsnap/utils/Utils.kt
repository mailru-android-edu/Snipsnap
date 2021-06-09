package com.wndenis.snipsnap.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}

fun extractName(filename: String?): String {
    filename?.let {
        val right = (filename.length - 5).coerceAtLeast(0)
        return "${filename.subSequence(0, right)}"
    }
    return ""
}

fun makeName(filename: String): String {
    return "$filename.spsp"
}
