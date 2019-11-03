package com.example.mediabender

import android.content.Intent
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
    private var musicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))

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
        if (musicPlaying) playButton.setImageResource(R.drawable.icons_pause_black)
        else playButton.setImageResource(R.drawable.icons_play_arrow_black)

        skipPlayingButton = findViewById(R.id.fastForwardButtMain)
        backPlayingButton = findViewById(R.id.fastRewindButtMain)
    }

    private fun addListenersOnButtons() {

        playButton.setOnClickListener {
            playPauseButtPressed()
        }

        skipPlayingButton.setOnClickListener {
            //displayToast("Skip")
            mediaControls.executeEvent(MediaEventType.SKIP_SONG,this)
        }

        backPlayingButton.setOnClickListener {
            //displayToast("Back")
            mediaControls.executeEvent(MediaEventType.PREVIOUS_SONG,this)
        }
    }


    private fun goToSettingsPage() {
        val intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // stay in app when back button pressed, so we do nothing
    }

    private fun playPauseButtPressed() {
        if (musicPlaying) {
            playButton.setImageResource(R.drawable.icons_play_arrow_black)
            //displayToast("Pause")
            mediaControls.executeEvent(MediaEventType.PAUSE,this)
            musicPlaying = false
        } else {
            playButton.setImageResource(R.drawable.icons_pause_black)
            //displayToast("Play")
            mediaControls.executeEvent(MediaEventType.PLAY,this)
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

    private fun loadResourcesForWhiteTheme(){

    }

    private fun loadResourcesForDarkTheme(){
        val root = mainActivity.rootView
        root.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
    }

}