package com.example.mediabender.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mediabender.R
import com.example.mediabender.dialogs.PlayerConnectionDialog
import com.example.mediabender.helpers.PlayerAccountSharedPreferenceHelper
import com.example.mediabender.helpers.PlayerSettingsCardViewHolder
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import com.example.mediabender.models.MediaPlayer
import com.example.mediabender.models.PlayerAccount
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity(), PlayerConnectionDialog.ConnectionDialogListener {

    private var spotifyViewHolder = PlayerSettingsCardViewHolder()
    private var appleMusicViewHolder = PlayerSettingsCardViewHolder()
    private var googlePlayViewHolder = PlayerSettingsCardViewHolder()
    private lateinit var settingsLabel: TextView
    private lateinit var accountsLabel: TextView
    private lateinit var themeSpinner: Spinner
    private lateinit var settingsActivity: View
    private lateinit var playerSharedPreferenceHelper: PlayerAccountSharedPreferenceHelper
    private var darkThemeChosen = false
    private var ledState = true
    private lateinit var installedPlayers: List<ApplicationInfo>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.elevation = 0f

        playerSharedPreferenceHelper = PlayerAccountSharedPreferenceHelper(
            getSharedPreferences(
                "Player Accounts",
                Context.MODE_PRIVATE
            )
        )

        setupSettings()
        setUpToolBar()
        loadAppropriateTheme()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        loadAppropriateTheme()
        finish()
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()

        getAllAppsOnPhone()
        setupApplePlayMusic()
        setupSpotify()
        setupGooglePlayMusic()
    }

    override fun onRestart() {
        super.onRestart()

        loadAppropriateTheme()
    }

    override fun acceptConnectionOnDismiss(name: MediaPlayer, playerAccount: PlayerAccount) {
        playerSharedPreferenceHelper.savePlayerAccount(playerAccount, name)
        when (name) {
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

    private fun setupSpotify() {
        spotifyViewHolder.cardView = findViewById(R.id.spotify_card)
        spotifyViewHolder.cardView.setOnClickListener {
             packageManager.getLaunchIntentForPackage(MediaPlayer.SPOTIFY.packageName)?.let { startActivity(it) }
        }

        //refactored for sw20
//        spotifyViewHolder.activeCircle = findViewById(R.id.spotify_active_circle)

        spotifyViewHolder.connectedTextView = findViewById(R.id.spotify_connection_textview)
        spotifyViewHolder.setConnectedTextWithEmail(
            playerSharedPreferenceHelper.getPlayerAccount(
                MediaPlayer.SPOTIFY
            )
        )

        if(MediaPlayer.SPOTIFY.packageName !in installedPlayers.map { player -> player.packageName }){
            spotifyViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupGooglePlayMusic() {
        googlePlayViewHolder.cardView = findViewById(R.id.google_play_card)
        googlePlayViewHolder.cardView.setOnClickListener {
            packageManager.getLaunchIntentForPackage(MediaPlayer.GOOGLE_PLAY.packageName)?.let { startActivity(it) }
        }

        //refactored for sw20
//        googlePlayViewHolder.activeCircle = findViewById(R.id.google_play_active_circle)

        googlePlayViewHolder.connectedTextView = findViewById(R.id.google_play_connection_textview)
        googlePlayViewHolder.setConnectedTextWithEmail(
            playerSharedPreferenceHelper.getPlayerAccount(
                MediaPlayer.GOOGLE_PLAY
            )
        )

        if(MediaPlayer.GOOGLE_PLAY.packageName !in installedPlayers.map { player -> player.packageName }){
            googlePlayViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupApplePlayMusic() {
        appleMusicViewHolder.cardView = findViewById(R.id.apple_music_card)
        appleMusicViewHolder.cardView.setOnClickListener {
            packageManager.getLaunchIntentForPackage(MediaPlayer.APPLE_MUSIC.packageName)?.let { startActivity(it) }
        }

        //refactored for sw20
//        appleMusicViewHolder.activeCircle = findViewById(R.id.apple_music_active_circle)

        appleMusicViewHolder.connectedTextView = findViewById(R.id.apple_music_connection_textview)
        appleMusicViewHolder.setConnectedTextWithEmail(
            playerSharedPreferenceHelper.getPlayerAccount(
                MediaPlayer.APPLE_MUSIC
            )
        )

        if(MediaPlayer.APPLE_MUSIC.packageName !in installedPlayers.map { player -> player.packageName }){
            appleMusicViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupSettings() {
        settingsLabel = findViewById(R.id.settingsTitle)
        settingsActivity = findViewById(R.id.settings_parent_scroll)
        accountsLabel = findViewById(R.id.accountsTitle)
        findViewById<Button>(R.id.settings_test_connection_button).setOnClickListener { testSensorConnection() }
        findViewById<Button>(R.id.settings_gesture_button).setOnClickListener { remapGesture() }
        findViewById<Switch>(R.id.settings_led_switch).setOnClickListener { ledSwitch() }

        //For the theme drop down menu
        themeSpinner = findViewById(R.id.settings_theme_spinner)

        themeSpinner.adapter = createArrayAdapterForSpinner()

        themeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    changeTheme(parent?.getItemAtPosition(position).toString())
                }
            }


    }

    private fun testSensorConnection() {
        //TODO implement later
        val toast = Toast.makeText(
            applicationContext,
            "Testing connection not implemented yet",
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

    private fun changeTheme(theme: String) {

        val themeHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))
        themeHelper.saveTheme(theme)

        darkThemeChosen = (theme == "Dark")
        loadAppropriateTheme()

        Toast.makeText(
            applicationContext,
            "$theme saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun remapGesture(){
        val intent = Intent(this,GestureMappingActivity::class.java)
        startActivity(intent)
    }


    private fun ledSwitch(){

        ledState = !(ledState)

        if (ledState == true){
            val toast = Toast.makeText(applicationContext,"Feedback Light On",
                    Toast.LENGTH_SHORT
            )
            toast.show()
        }
        else {
            val toast = Toast.makeText(applicationContext,"Feedback Light Off",
                    Toast.LENGTH_SHORT
            )
            toast.show()
        }



    }


    fun setRunningIndicator(playerPackage: MediaPlayer, active: Boolean) {
        //function definition refactored for SW20
//        val resource =
//            if (active) R.drawable.active_circle_account else R.drawable.inactive_circle_account
//
//        when (playerPackage) {
//            MediaPlayer.SPOTIFY -> {
//                spotifyViewHolder.activeCircle.setImageResource(resource)
//            }
//            MediaPlayer.APPLE_MUSIC -> {
//                appleMusicViewHolder.activeCircle.setImageResource(resource)
//            }
//            MediaPlayer.GOOGLE_PLAY -> {
//                googlePlayViewHolder.activeCircle.setImageResource(resource)
//            }
//        }
//
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setUpToolBar() {
        //actionbar
        val actionbar = supportActionBar
        //set actionbar title

        //set back button
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.title = ""
    }

    private fun loadWhiteTheme() {
        settings_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        settings_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_black)
        settingsLabel.setTextColor(getColor(R.color.colorPrimaryDark))
        accountsLabel.setTextColor(getColor(R.color.colorPrimaryDark))
        window.statusBarColor = getColor(R.color.whiteForStatusBar)
        settingsActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))

    }

    private fun loadDarkTheme() {
        settings_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        settings_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_white)
        settingsLabel.setTextColor(getColor(R.color.colorPrimaryWhite))
        accountsLabel.setTextColor(getColor(R.color.colorPrimaryWhite))
        window.statusBarColor = getColor(R.color.colorPrimaryDark)
        settingsActivity.setBackgroundColor(getColor(R.color.colorPrimaryDark))
    }

    private fun loadAppropriateTheme() {
        val currentMode =
            settingsActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK


        if (currentMode == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) loadDarkTheme()
        else loadWhiteTheme()
    }

    // To make the drop down menu for Themes to show the right saved theme
    private fun createArrayAdapterForSpinner(): ArrayAdapter<CharSequence> {
        val themeHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))
        val savedTheme = themeHelper.getTheme()

        when (savedTheme) {
            "Dark" -> {
                darkThemeChosen = true
                return ArrayAdapter.createFromResource(
                    this,
                    R.array.themesDarkSaved,
                    R.layout.support_simple_spinner_dropdown_item
                )
            }
            else -> {
                darkThemeChosen = false
                return ArrayAdapter.createFromResource(
                    this,
                    R.array.themesLightSaved,
                    R.layout.support_simple_spinner_dropdown_item
                )
            }

        }

    }

    fun getAllAppsOnPhone(){
        this.installedPlayers = packageManager.getInstalledApplications(0).filter { it.packageName in MediaPlayer.values().map { player -> player.packageName } }
        Log.d("Installed apps","Got installed apps")
    }


}

