package com.example.mediabender

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
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
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
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
    private lateinit var gestureDecoder: GestureEventDecoder
    private lateinit var menu_main: Menu
    private var darkThemeChosen = false
    private var musicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        gestureDecoder = GestureEventDecoder(applicationContext)

        setChosenTheme()
        setUpToolbar()
        initSerialCommunication()
        initMediaControls()
        initViews()
        addListenersOnButtons()

    }

    override fun onDestroy() {
        metadataHelper.unregisterBroadcastReceiver()
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        setChosenTheme()
        if ((mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) loadResourcesForDarkTheme()
        else loadResourcesForWhiteTheme()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu_main = menu!!

        when ((mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) {
            false -> loadResourcesForWhiteTheme()
            true -> loadResourcesForDarkTheme()
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.moveToSettingsAction) {
            goToSettingsPage()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onPause() {
        super.onPause()

        SerialCommunicationService.instance.removeDataOnReceiveListener()
    }
    override fun onResume() {
        super.onResume()

        SerialCommunicationService.instance.setDataOnReceiveListener {
            runOnUiThread {
                val event = gestureDecoder.gestureToEvent(it.gesture)
                Toast.makeText(applicationContext,"Got gesture: ${it.gesture.toString} -> ${event.name}", Toast.LENGTH_SHORT).show()
                mediaControls.executeEvent(event, this)
            }
        }

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

        if (musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO || darkThemeChosen) playButton.setImageResource(
            R.drawable.icons_pause_black
        )
        else if (musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) playButton.setImageResource(
            R.drawable.icons_pause_white
        )
        else if (!musicPlaying && (mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO || darkThemeChosen) playButton.setImageResource(
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
            mediaControls.executeEvent(MediaEventType.SKIP_SONG)
        }

        backPlayingButton.setOnClickListener {
            mediaControls.executeEvent(MediaEventType.PREVIOUS_SONG)
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

            playButton.setImageResource(R.drawable.icons_play_arrow_white)

            //displayToast("Pause")
            mediaControls.executeEvent(MediaEventType.PAUSE)
            musicPlaying = false
        } else {
            playButton.setImageResource(R.drawable.icons_pause_white)


            mediaControls.executeEvent(MediaEventType.PLAY)
            musicPlaying = true
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        setChosenTheme()
        val currentNightMode =
            mainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) {
            loadResourcesForDarkTheme()

        } // Night mode is not active, we're using the light theme
        else {
            loadResourcesForWhiteTheme()

        } // Night mode is active, we're using dark theme
    }


    private fun loadResourcesForDarkTheme() {
        if (musicPlaying) playButton.setImageResource(R.drawable.icons_pause_white)
        else playButton.setImageResource(R.drawable.icons_play_arrow_white)

        playButton.background = getDrawable(R.drawable.red_round_button_background)
        skipPlayingButton.setImageResource(R.drawable.icons_fast_forward_white)
        backPlayingButton.setImageResource(R.drawable.icons_fast_rewind_white)
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        val toolbar = supportActionBar
        //Toolbar colour
        toolbar?.setBackgroundDrawable(getDrawable(R.color.colorPrimaryDark))

        menu_main?.getItem(0).setIcon(getDrawable(R.drawable.icons_settings_white))
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        songTitleTV.setTextColor(getColor(R.color.colorPrimaryWhite))
        artistNameMainTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        window.statusBarColor = getColor(R.color.colorPrimaryDark)
    }

    private fun loadResourcesForWhiteTheme() {
        playButton.background = getDrawable(R.drawable.black_round_button_black)
        if (musicPlaying) playButton.setImageResource(R.drawable.icons_pause_white)
        else playButton.setImageResource(R.drawable.icons_play_arrow_white)
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        skipPlayingButton.setImageResource(R.drawable.icons_fast_forward_black)
        backPlayingButton.setImageResource(R.drawable.icons_fast_rewind_black)
        songTitleTV.setTextColor(getColor(R.color.colorPrimaryDark))
        artistNameMainTextView?.setTextColor(getColor(R.color.colorPrimaryDark))
        mainActivity.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.colorPrimaryWhite))
        menu_main?.getItem(0).setIcon(getDrawable(R.drawable.icons_settings_black))
        window.statusBarColor = getColor(R.color.whiteForStatusBar)
    }

    // display the track, artist and album art on main screen
    // album_art == null -> art will not change
    // album_art != null -> art will be updated
    fun displayCurrentSong(track: String, artist: String) {
        songTitleTV.text = track
        songArtistNameTV.text = artist
        albumArt.setImageDrawable(getDrawable(R.drawable.placeholder_song))
    }

    fun changeCoverArt(bitmap: Bitmap?){
        bitmap?.let {
            albumArt.setImageBitmap(it)
        }
    }

    // update the playback state (both the internal boolean and the views)
    fun updatePlaybackState(playing: Boolean) {
        musicPlaying = playing  // change state

        if (musicPlaying) {
            playButton.setImageResource(R.drawable.icons_pause_white)

        } else {
            playButton.setImageResource(R.drawable.icons_play_arrow_white)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.elevation = 0f
        supportActionBar?.title = ""
    }

    private fun setChosenTheme() {
        val themeHelper =
            ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))
        val chosenTheme = themeHelper.getTheme()

        when (chosenTheme) {
            "Light" -> darkThemeChosen = false
            "Dark" -> darkThemeChosen = true
            null -> darkThemeChosen = false
        }
    }
}
