package com.example.mediabender.activities

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediabender.R
import com.example.mediabender.dialogs.PlayerConnectionDialog
import com.example.mediabender.helpers.PlayerAccountSharedPreferenceHelper
import com.example.mediabender.helpers.PlayerSettingsCardViewHolder
import com.example.mediabender.models.MediaPlayer
import com.example.mediabender.models.PlayerAccount


class SettingsActivity : AppCompatActivity(), PlayerConnectionDialog.ConnectionDialogListener {

    private var spotifyViewHolder = PlayerSettingsCardViewHolder()
    private var appleMusicViewHolder = PlayerSettingsCardViewHolder()
    private var googlePlayViewHolder = PlayerSettingsCardViewHolder()
    private lateinit var playerSharedPreferenceHelper: PlayerAccountSharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))

        playerSharedPreferenceHelper = PlayerAccountSharedPreferenceHelper(getSharedPreferences("Player Accounts", Context.MODE_PRIVATE))

        if(!checkIfPermissionGranted())
            startActivity( Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

        setupSettings()
        setupApplePlayMusic()
        setupSpotify()
        setupGooglePlayMusic()
    }

    override fun onResume() {
        super.onResume()

        val running = getRunningPlayers()

        MediaPlayer.values().forEach {
            if(running.contains(it.packageName)){
                setRunningIndicator(it, true)
            }else{
                setRunningIndicator(it, false)
            }
        }
    }

    override fun acceptConnectionOnDismiss(name: MediaPlayer, playerAccount: PlayerAccount) {
        playerSharedPreferenceHelper.savePlayerAccount(playerAccount, name)
        when(name){
            MediaPlayer.SPOTIFY -> {
                setupSpotify()
            }
            MediaPlayer.APPLE_MUSIC -> {
                setupApplePlayMusic()
            }
            MediaPlayer.GOOGLE_PLAY -> {
                setupGooglePlayMusic()
            }
        }
    }

    private fun checkIfPermissionGranted(): Boolean{
        var granted = false;
        val appOps = applicationContext
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), applicationContext.getPackageName()
        )

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted =
                applicationContext.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) === PackageManager.PERMISSION_GRANTED
        } else {
            granted = mode == AppOpsManager.MODE_ALLOWED
        }
        return granted
    }

    private fun setupSpotify(){
        spotifyViewHolder.cardView = findViewById(R.id.spotify_card)
        spotifyViewHolder.cardView.setOnClickListener {
            val dialog = PlayerConnectionDialog(MediaPlayer.SPOTIFY)
            dialog.show(supportFragmentManager, "Spotify Connection")
        }

        spotifyViewHolder.activeCircle = findViewById(R.id.spotify_active_circle)

        spotifyViewHolder.connectedTextView = findViewById(R.id.spotify_connection_textview)
        spotifyViewHolder.setConnectedTextWithEmail(playerSharedPreferenceHelper.getPlayerAccount(MediaPlayer.SPOTIFY))
    }
    private fun setupGooglePlayMusic(){
        googlePlayViewHolder.cardView = findViewById(R.id.google_play_card)
        googlePlayViewHolder.cardView.setOnClickListener {
            val dialog = PlayerConnectionDialog(MediaPlayer.GOOGLE_PLAY)
            dialog.show(supportFragmentManager, "Google Play Connection")
        }

        googlePlayViewHolder.activeCircle = findViewById(R.id.google_play_active_circle)

        googlePlayViewHolder.connectedTextView = findViewById(R.id.google_play_connection_textview)
        googlePlayViewHolder.setConnectedTextWithEmail(playerSharedPreferenceHelper.getPlayerAccount(MediaPlayer.GOOGLE_PLAY))
    }
    private fun setupApplePlayMusic(){
        appleMusicViewHolder.cardView = findViewById(R.id.apple_music_card)
        appleMusicViewHolder.cardView.setOnClickListener {
            val dialog = PlayerConnectionDialog(MediaPlayer.APPLE_MUSIC)
            dialog.show(supportFragmentManager, "Apple Music Connection")
        }

        appleMusicViewHolder.activeCircle = findViewById(R.id.apple_music_active_circle)

        appleMusicViewHolder.connectedTextView = findViewById(R.id.apple_music_connection_textview)
        appleMusicViewHolder.setConnectedTextWithEmail(playerSharedPreferenceHelper.getPlayerAccount(MediaPlayer.APPLE_MUSIC))
    }

    private fun setupSettings(){
        findViewById<Button>(R.id.settings_test_connection_button).setOnClickListener { testSensorConnection() }
        findViewById<Button>(R.id.settings_gesture_button).setOnClickListener { remapGesture() }
        findViewById<Spinner>(R.id.settings_theme_spinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                changeTheme("DEFAULT")
            }
        }
    }

    private fun testSensorConnection(){
        //TODO implement later
        val toast = Toast.makeText(applicationContext,"Testing connection not implemented yet", Toast.LENGTH_SHORT)
        toast.show()
    }
    private fun changeTheme(theme: String){
        //TODO implement later
        val toast = Toast.makeText(applicationContext,"Changing theme not implemented yet", Toast.LENGTH_SHORT)
        toast.show()
    }
    private fun remapGesture(){
        //TODO implement later
        val toast = Toast.makeText(applicationContext,"Gesture mapping not implemented yet", Toast.LENGTH_SHORT)
        toast.show()
    }

    fun setRunningIndicator(playerPackage: MediaPlayer, active: Boolean){
        val resource = if(active) R.drawable.active_circle_account else R.drawable.inactive_circle_account

        when(playerPackage){
            MediaPlayer.SPOTIFY -> {
                spotifyViewHolder.activeCircle.setImageResource(resource)
            }
            MediaPlayer.APPLE_MUSIC -> {
                appleMusicViewHolder.activeCircle.setImageResource(resource)
            }
            MediaPlayer.GOOGLE_PLAY -> {
                googlePlayViewHolder.activeCircle.setImageResource(resource)
            }
        }

    }

    fun getRunningPlayers(): Set<String>{
        //TODO implement with apis
        return HashSet()
    }
}
