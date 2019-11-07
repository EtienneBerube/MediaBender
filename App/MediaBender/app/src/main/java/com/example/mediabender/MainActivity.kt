package com.example.mediabender

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.mediabender.activities.SettingsActivity
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Request
import com.example.mediabender.service.Sensibility
import com.example.mediabender.service.SerialCommunicationService
import com.example.mediabender.service.ServiceRequest

class MainActivity : AppCompatActivity() {

    private lateinit var mediaControls: MediaControls
    private lateinit var metadataHelper: MetadataHelper

    private lateinit var albumArt: ImageView
    private lateinit var songTitleTV: TextView
    private lateinit var songArtistNameTV: TextView
    private lateinit var playButton: ImageButton
    private lateinit var skipPlayingButton: ImageButton
    private lateinit var backPlayingButton: ImageButton
    private lateinit var gestureDecoder: GestureEventDecoder

    private var musicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMediaControls()
        initSerialCommunication()
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

    override fun onPause() {
        super.onPause()

        SerialCommunicationService.instance.removeDataOnReceiveListener()
    }

    private fun initSerialCommunication() {
        gestureDecoder = GestureEventDecoder(applicationContext)

        SerialCommunicationService.instance.setService(this)
        if(!SerialCommunicationService.instance.isConnected){
            SerialCommunicationService.instance.requestUSBpermission(this)
        }
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

        musicPlaying = mediaControls.isMusicPlaying()
        playButton = findViewById(R.id.playPauseButtMain)
        if (musicPlaying) playButton.setImageResource(R.drawable.pause_white)
        else playButton.setImageResource(R.drawable.play_arrow_white)

        skipPlayingButton = findViewById(R.id.fastForwardButtMain)
        backPlayingButton = findViewById(R.id.fastRewindButtMain)
    }

    private fun addListenersOnButtons() {

        playButton.setOnClickListener {
            playPauseButtPressed()
        }

        skipPlayingButton.setOnClickListener {
            //displayToast("Skip")
            mediaControls.next()
        }

        backPlayingButton.setOnClickListener {
            //displayToast("Back")
            mediaControls.previous()
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
        if (musicPlaying) { // if playing, update view to paused mode, and pause media
            updatePlaybackState(false)
            mediaControls.executeEvent(MediaEventType.PAUSE,this)
        } else {    // if not playing, update view to playing mode, and play media
            updatePlaybackState(true)
            mediaControls.executeEvent(MediaEventType.PLAY,this)
        }
        SerialCommunicationService.instance.sendRequest(ServiceRequest(Request.FLAG))
    }

    // display the track, artist and album art on main screen
    // album_art == null -> art will not change
    // album_art != null -> art will be updated
    fun displayCurrentSong(track: String, artist: String, album_art: Bitmap?) {
        songTitleTV.text = track
        songArtistNameTV.text = artist
        if (album_art != null) albumArt.setImageBitmap(album_art)
    }

    // update the playback state (both the internal boolean and the views)
    fun updatePlaybackState(playing: Boolean) {
        musicPlaying = playing  // change state
        if (musicPlaying) { // if currently playing, set to pause button
            playButton.setImageResource(R.drawable.pause_white)
        } else {    // if currently paused, set to play button
            playButton.setImageResource(R.drawable.play_arrow_white)
        }
    }

    private fun displayToast(buttonName: String) {
        Toast.makeText(
            this,
            "$buttonName button pressed, but not implemented yet",
            Toast.LENGTH_SHORT
        ).show()
    }
}
