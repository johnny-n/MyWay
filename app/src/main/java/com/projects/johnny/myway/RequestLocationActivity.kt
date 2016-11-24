package com.projects.johnny.myway

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_request_location.*

/*
    This class simply exists to tell users to turn on their locations setting.
 */
class RequestLocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_location)

        signOutButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.setFlags(intent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.setFlags(intent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
        finish()
    }
}