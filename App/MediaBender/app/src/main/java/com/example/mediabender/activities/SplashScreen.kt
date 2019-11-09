package com.example.mediabender.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.mediabender.MainActivity
import com.example.mediabender.R

class SplashScreen : AppCompatActivity() {

    private val Splash_Timeout:Long=1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        Handler().postDelayed({

            startActivity(Intent(this, MainActivity::class.java))
            finish()

        },Splash_Timeout)
    }
}
