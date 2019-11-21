package com.example.mediabender.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.mediabender.MainActivity
import com.example.mediabender.R
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {

    private val Splash_Timeout: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        loadAppropriateTheme()
    }

    override fun onStart() {

        super.onStart()

        Handler().postDelayed({

            showCarefulNotice()

        }, Splash_Timeout)
    }

    private fun loadAppropriateTheme() {
        val themeHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))

        val mode = themeHelper.getTheme()
        val currentMode =
            splashScreen.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentMode == Configuration.UI_MODE_NIGHT_YES || mode == "Dark") {
            false -> loadWhiteTheme()
            true -> loadDarkTheme()
        }

    }

    private fun loadWhiteTheme() {
        window.statusBarColor = getColor(R.color.colorPrimaryWhite)
        splashScreen.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
    }

    private fun loadDarkTheme() {
        window.statusBarColor = getColor(R.color.colorPrimaryDark)
        splashScreen.setBackgroundColor(getColor(R.color.colorPrimaryDark))
    }

    private fun showCarefulNotice(){
        val preferences = getSharedPreferences("careful", Context.MODE_PRIVATE)
        val firstOpening = preferences.getBoolean("first_opening", true)
        preferences.edit().putBoolean("first_opening", false).apply()

        if(firstOpening){
            val builder = AlertDialog.Builder(this@SplashScreen)

            builder.setTitle("Be careful")

            builder.setMessage("This app aims to reduce the amount of distractions while driving. If you need to interact with your phone or if a gesture did not work properly, always put your safety first and stop the car before interacting with your device.")

            builder.setPositiveButton("I will be careful and respect the law"){dialog, which ->
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()
        }else{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

