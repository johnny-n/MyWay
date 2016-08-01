package com.projects.johnny.myway

import android.app.Application

class App : Application() {
    companion object {
        var UID: String? = null
        val locationRequestCode = 2
    }
}