package com.projects.johnny.myway

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity() {

    companion object {
        val locationRequestCode = 2
    }

    var backEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, DirectionsFragment.newInstance(null)) // TODO: Pass credentials to directionsFragment?
                        .commit()
    }

    override fun onBackPressed() {
        if (backEnabled) return else super.onBackPressed()
    }
}