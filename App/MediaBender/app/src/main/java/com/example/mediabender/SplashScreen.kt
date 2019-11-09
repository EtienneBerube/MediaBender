package com.example.mediabender

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.lang.Exception

class SplashScreen : AppCompatActivity() {

    private val Splash_Timeout: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        loadAppropriateTheme()

        Handler().postDelayed({

            startActivity(Intent(this, MainActivity::class.java))
            finish()

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
}
