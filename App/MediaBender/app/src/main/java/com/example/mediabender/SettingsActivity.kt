package com.example.mediabender

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    private fun setupSpotify(){}
    private fun setupGooglePlayMusic(){}
    private fun setupApplePlayMusic(){}
    private fun testSensorConnection(){}
    private fun changeTheme(theme: String){}
    private fun remapGesture(){}

    fun isNamedProcessRunning(processName: String?): Boolean {
        if (processName == null)
            return false

        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        for (process in processes) {
            if (processName == process.processName) {
                return true
            }
        }
        return false
    }
}
