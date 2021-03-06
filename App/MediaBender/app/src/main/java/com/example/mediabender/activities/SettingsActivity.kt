package com.example.mediabender.activities

//import com.example.mediabender.helpers.PlayerAccountSharedPreferenceHelper
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediabender.R
import com.example.mediabender.helpers.PlayerSettingsCardViewHolder
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import com.example.mediabender.models.MediaPlayer
import com.example.mediabender.service.Request
import com.example.mediabender.service.Sensibility
import com.example.mediabender.service.SerialCommunicationService
import com.example.mediabender.service.ServiceRequest
import com.shrikanthravi.library.NightModeButton
import kotlinx.android.synthetic.main.activity_settings.*
import nl.dionsegijn.steppertouch.OnStepCallback
import nl.dionsegijn.steppertouch.StepperTouch

/**
 * Activity that holds all the customization and settings for the app.
 */
class SettingsActivity : AppCompatActivity() {

    private var spotifyViewHolder = PlayerSettingsCardViewHolder()
    private var appleMusicViewHolder = PlayerSettingsCardViewHolder()
    private var googlePlayViewHolder = PlayerSettingsCardViewHolder()
    private lateinit var settingsLabel: TextView
    private lateinit var accountsLabel: TextView
    private lateinit var settingsActivity: View
    private lateinit var nightModeButton: NightModeButton
//    private lateinit var playerSharedPreferenceHelper: PlayerAccountSharedPreferenceHelper
    private lateinit var stepperTouch: StepperTouch
    private var darkThemeChosen = true
    private lateinit var installedPlayers: List<ApplicationInfo>
    private var ledState = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.elevation = 0f

        getThemeSaved()
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

    private fun setupSpotify() {
        spotifyViewHolder.cardView = findViewById(R.id.spotify_card)
        spotifyViewHolder.cardView.setOnClickListener {
            packageManager.getLaunchIntentForPackage(MediaPlayer.SPOTIFY.packageName)
                ?.let { startActivity(it) }
        }

        if (MediaPlayer.SPOTIFY.packageName !in installedPlayers.map { player -> player.packageName }) {
            spotifyViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupGooglePlayMusic() {
        googlePlayViewHolder.cardView = findViewById(R.id.google_play_card)
        googlePlayViewHolder.cardView.setOnClickListener {
            packageManager.getLaunchIntentForPackage(MediaPlayer.GOOGLE_PLAY.packageName)
                ?.let { startActivity(it) }
        }

        if (MediaPlayer.GOOGLE_PLAY.packageName !in installedPlayers.map { player -> player.packageName }) {
            googlePlayViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupApplePlayMusic() {
        appleMusicViewHolder.cardView = findViewById(R.id.apple_music_card)
        appleMusicViewHolder.cardView.setOnClickListener {
            packageManager.getLaunchIntentForPackage(MediaPlayer.APPLE_MUSIC.packageName)
                ?.let { startActivity(it) }
        }

        if (MediaPlayer.APPLE_MUSIC.packageName !in installedPlayers.map { player -> player.packageName }) {
            appleMusicViewHolder.cardView.visibility = View.GONE
        }
    }

    private fun setupSettings() {
        settingsLabel = findViewById(R.id.settingsTitle)
        settingsActivity = findViewById(R.id.settings_parent_scroll)
        accountsLabel = findViewById(R.id.accountsTitle)
        stepperTouch = findViewById(R.id.StepperTouch)
        nightModeButton = findViewById(R.id.nightModeButton)
        nightModeButton.setModeFirstTime(darkThemeChosen)

        findViewById<Button>(R.id.settings_test_connection_button).setOnClickListener { testSensorConnection() }
        findViewById<Button>(R.id.settings_gesture_button).setOnClickListener { remapGesture() }

        nightMode()
        setUpStepper()

    }

    private fun testSensorConnection() {
        if(SerialCommunicationService.instance.testUSBConnection()){
            val toast = Toast.makeText(
                applicationContext,
                "The sensor is connected",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }else{
            val toast = Toast.makeText(
                applicationContext,
                "The sensor is not connected",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }


    private fun remapGesture() {
        val intent = Intent(this, GestureMappingActivity::class.java)
        startActivity(intent)
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
        settings_card.setCardBackgroundColor(getColor(R.color.whiteForStatusBar))
        window.statusBarColor = getColor(R.color.whiteForStatusBar)
        settingsActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))

    }

    private fun loadDarkTheme() {
        settings_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        settings_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_white)
        settingsLabel.setTextColor(getColor(R.color.colorPrimaryWhite))
        accountsLabel.setTextColor(getColor(R.color.colorPrimaryWhite))
        settings_card.setCardBackgroundColor(getColor(R.color.darkForCard))
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
    private fun getThemeSaved() {
        val themeHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))
        val savedTheme = themeHelper.getTheme()

        when (savedTheme) {
            "Dark" -> {
                darkThemeChosen = true

            }
            else -> {
                darkThemeChosen = false

            }
        }
    }

    fun getAllAppsOnPhone() {
        this.installedPlayers = packageManager.getInstalledApplications(0)
            .filter { it.packageName in MediaPlayer.values().map { player -> player.packageName } }
        Log.d("Installed apps", "Got installed apps")
    }

    fun nightMode() {

        nightModeButton.setOnSwitchListener {

            if (darkThemeChosen) {

                darkThemeChosen = false

                if (settingsActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {

                    Toast.makeText(
                        getApplicationContext(),
                        "Dark Theme will remain because Low Power Mode is Enabled to save energy",
                        Toast.LENGTH_LONG
                    ).show();

                }
                saveTheme(darkThemeChosen)

            } else {

                darkThemeChosen = true
                saveTheme(darkThemeChosen)
            }
            loadAppropriateTheme()

        }
    }

    private fun saveTheme(darkModeSaved: Boolean) {
        val themeSharedPreferenceHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))
        when (darkModeSaved) {
            true -> {
                themeSharedPreferenceHelper.saveTheme("Dark")
            }
            false -> {
                themeSharedPreferenceHelper.saveTheme("Light")
            }
        }
    }

    private fun setUpStepper() {
        //define minimum value of sensitivity stepper to 0
        stepperTouch.minValue = 1
        //allow side taps
        stepperTouch.sideTapEnabled = true
        //set maximum value of sensitivity stepper to 4
        stepperTouch.maxValue = 3

        //add callback for the sensitivity stepper
        stepperTouch.addStepCallback(
            object : OnStepCallback {
                override fun onStep(value: Int, positive: Boolean) {
                    //switch statement to initiate action when the stepper changes the value
                    //add code to each case to tell arduino to switch sensitivity
                    when (value) {
                        1 -> {
                            if (SerialCommunicationService.instance.isConnected) {
                                SerialCommunicationService.instance.sendRequest(
                                    ServiceRequest(Request.SENSIBILITY, Sensibility.LOW)
                                )
                            } else {
                                Toast.makeText(applicationContext, "Connect the sensor first", Toast.LENGTH_SHORT).show()
                            }
                            Toast.makeText(
                                applicationContext,
                                "Low sensitivity",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        2 -> {
                            if (SerialCommunicationService.instance.isConnected) {
                                SerialCommunicationService.instance.sendRequest(
                                    ServiceRequest(
                                        Request.SENSIBILITY,
                                        Sensibility.MEDIUM
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Connect the sensor first",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Toast.makeText(
                                applicationContext,
                                "Medium sensitivity",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        3 -> {
                            if (SerialCommunicationService.instance.isConnected) {
                                SerialCommunicationService.instance.sendRequest(
                                    ServiceRequest(
                                        Request.SENSIBILITY,
                                        Sensibility.HIGH
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Connect the sensor first",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Toast.makeText(
                                applicationContext,
                                "High sensitivity",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        )

    }
}

