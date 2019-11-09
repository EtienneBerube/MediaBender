package com.example.mediabender

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.example.mediabender.activities.SettingsActivity
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.SerialCommunicationService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mediaControls: MediaControls
    private lateinit var metadataHelper: MetadataHelper
    private lateinit var mainActivity: View
    private lateinit var albumArt: ImageView
    private lateinit var songTitleTV: TextView
    private lateinit var songArtistNameTV: TextView
    private lateinit var playButton: ImageButton
    private lateinit var skipPlayingButton: ImageButton
    private lateinit var backPlayingButton: ImageButton
    private lateinit var menu_main: Menu
    private var musicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.main_toolbar))

        initSerialCommunication()
        initMediaControls()
        initViews()
        addListenersOnButtons()

    }

    override fun onDestroy() {
        metadataHelper.unregisterBroadcastReceiver()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu_main = menu!!

        when ((mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> loadResourcesForWhiteTheme()
            Configuration.UI_MODE_NIGHT_YES -> loadResourcesForDarkTheme()
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.moveToSettingsAction) {
            goToSettingsPage()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initSerialCommunication() {
        SerialCommunicationService.instance.setService(this)
        SerialCommunicationService.instance.requestUSBpermission(applicationContext)
    }

    // cannot initialize the MediaControls object before the onCreate because it calls
    // getSystemService in its construction, which is not available before onCreate is called
    private fun initMediaControls() {
        mediaControls = MediaControls(this)
        metadataHelper = MetadataHelper(this)
    }

    private fun initViews() {
        albumArt = findViewById(R.id.albumArtViewer)
        songTitleTV = findViewById(R.id.songTitleMainTextView)
        songArtistNameTV = findViewById(R.id.artistNameMainTextView)
        mainActivity = findViewById(R.id.mainActivity)
        musicPlaying = mediaControls.isMusicPlaying()
        playButton = findViewById(R.id.playPauseButtMain)

        if (musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) playButton.setImageResource(
            R.drawable.icons_pause_black
        )
        else if (musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) playButton.setImageResource(
            R.drawable.icons_pause_white
        )
        else if (!musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) playButton.setImageResource(
            R.drawable.icons_play_arrow_black
        )
        else playButton.setImageResource(R.drawable.icons_play_arrow_white)

        skipPlayingButton = findViewById(R.id.fastForwardButtMain)
        backPlayingButton = findViewById(R.id.fastRewindButtMain)
    }

    private fun addListenersOnButtons() {

        playButton.setOnClickListener {
            playPauseButtPressed()
        }

        skipPlayingButton.setOnClickListener {
            //displayToast("Skip")
            mediaControls.executeEvent(MediaEventType.SKIP_SONG, this)
        }

        backPlayingButton.setOnClickListener {
            //displayToast("Back")
            mediaControls.executeEvent(MediaEventType.PREVIOUS_SONG, this)
        }
    }


    private fun goToSettingsPage() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // stay in app when back button pressed, so we do nothing
    }

    private fun playPauseButtPressed() {
        if (musicPlaying) {

            when ((mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_NO -> playButton.setImageResource(R.drawable.icons_play_arrow_black)
                Configuration.UI_MODE_NIGHT_YES -> playButton.setImageResource(R.drawable.icons_play_arrow_white)
            }

            //displayToast("Pause")
            mediaControls.executeEvent(MediaEventType.PAUSE, this)
            musicPlaying = false
        } else {

            when ((mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_NO -> playButton.setImageResource(R.drawable.icons_pause_black)
                Configuration.UI_MODE_NIGHT_YES -> playButton.setImageResource(R.drawable.icons_pause_white)
            }
            //displayToast("Play")
            mediaControls.executeEvent(MediaEventType.PLAY, this)
            musicPlaying = true
        }
    }

    private fun displayToast(buttonName: String) {
        Toast.makeText(
            this,
            "$buttonName button pressed, but not implemented yet",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val currentNightMode = mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            loadResourcesForWhiteTheme()

        } // Night mode is not active, we're using the light theme
        else {
            loadResourcesForDarkTheme()

        } // Night mode is active, we're using dark theme
    }


    private fun loadResourcesForDarkTheme() {
        if (musicPlaying) playButton.setImageResource(R.drawable.icons_pause_white)
        else playButton.setImageResource(R.drawable.icons_play_arrow_white)

        skipPlayingButton.setImageResource(R.drawable.icons_fast_forward_white)
        backPlayingButton.setImageResource(R.drawable.icons_fast_rewind_white)
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        val toolbar = supportActionBar
        toolbar?.setBackgroundDrawable(getDrawable(R.color.colorPrimaryDark))
        main_toolbar.setTitleTextColor(getColor(R.color.colorPrimaryWhite))
        menu_main.getItem(0).setIcon(getDrawable(R.drawable.icons_settings_white))
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        songTitleTV.setTextColor(getColor(R.color.colorPrimaryWhite))
        artistNameMainTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
    }

    private fun loadResourcesForWhiteTheme() {
        if (musicPlaying) playButton.setImageResource(R.drawable.icons_pause_black)
        else playButton.setImageResource(R.drawable.icons_pause_black)
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        skipPlayingButton.setImageResource(R.drawable.icons_fast_forward_black)
        backPlayingButton.setImageResource(R.drawable.icons_fast_rewind_black)
        songTitleTV.setTextColor(getColor(R.color.colorPrimaryDark))
        artistNameMainTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.colorPrimaryWhite))
        main_toolbar.setTitleTextColor(getColor(R.color.colorPrimaryDark))
        menu_main.getItem(0).setIcon(getDrawable(R.drawable.icons_settings_black))
    }

}
