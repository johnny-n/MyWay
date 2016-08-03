package com.projects.johnny.myway

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity() {

    companion object {
        val locationRequestCode = 2
    }

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

        val count = fragmentManager.backStackEntryCount

        // pops the back stack if there are fragments alive there
        // do nothing otherwise (meaning, dont exit app if we're inside DirectionsFragment
        if (count == 0) null else fragmentManager.popBackStack()
    }
}