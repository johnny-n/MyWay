package com.projects.johnny.myway

import android.content.Context
import android.widget.Toast

fun Context.makeText(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}