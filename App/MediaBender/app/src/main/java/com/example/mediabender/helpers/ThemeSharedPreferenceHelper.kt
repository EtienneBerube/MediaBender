package com.example.mediabender.helpers

import android.content.SharedPreferences

/**
 * This class is used as a shard preference wrapper to get the current theme and modify it
 */
class ThemeSharedPreferenceHelper(var sharedPreferences: SharedPreferences) {

    fun saveTheme (themeChosen: String){
        val editor = sharedPreferences.edit()
        editor.putString("Theme", themeChosen)
        editor.apply()
    }

    fun getTheme(): String? {
        return sharedPreferences.getString("Theme",null)
    }

}