package com.example.mediabender.helpers

import android.content.Context
import com.example.mediabender.R
import com.example.mediabender.models.MediaPlayer
import com.example.mediabender.models.PlayerAccount


class PlayerAccountSharedPreferenceHelper(var context: Context) {

    var sharedPreferences = context.getSharedPreferences("Player Accounts", Context.MODE_PRIVATE)

    fun getPlayerAccount(player: MediaPlayer): PlayerAccount?{
        val email = sharedPreferences.getString("${player.packageName}_email", null)
        val password = sharedPreferences.getString("${player.packageName}_password", null)

        if(email != null && password != null)
            return PlayerAccount(email, password)
        else
            return null
    }
    fun savePlayerAccount(account: PlayerAccount?, player: MediaPlayer) {
        val editor = sharedPreferences.edit()
        editor.putString("${player.packageName}_email", account?.email)
        editor.putString("${player.packageName}_password", account?.password)
        editor.apply()
    }

    fun deletePlayerAccount( player: MediaPlayer){
        savePlayerAccount(null, player)
    }
}