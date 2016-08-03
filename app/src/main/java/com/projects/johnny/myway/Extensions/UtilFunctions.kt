package com.projects.johnny.myway.Extensions

import android.app.Activity
import android.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.projects.johnny.myway.MainActivity

fun replaceFragment(activity: Activity, containerId: Int, fragment: Fragment) {
    activity.fragmentManager.beginTransaction()
                            .replace(containerId, fragment)
                            .addToBackStack(null)
                            .commit()
}
