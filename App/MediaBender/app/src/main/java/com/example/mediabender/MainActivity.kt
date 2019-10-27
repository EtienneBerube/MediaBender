package com.example.mediabender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
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

        initViews()
        addListenersOnButtons()
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

    private fun initViews() {
        albumArt = findViewById(R.id.albumArtViewer)
        songTitleTV = findViewById(R.id.songTitleMainTextView)
        songArtistNameTV = findViewById(R.id.artistNameMainTextView)
        playButton = findViewById(R.id.playPauseButtMain)
        playButton.setImageResource(R.drawable.pause_white)

        skipPlayingButton = findViewById(R.id.fastForwardButtMain)
        backPlayingButton = findViewById(R.id.fastRewindButtMain)
    }

    private fun addListenersOnButtons() {

        playButton.setOnClickListener {
            playPauseButtPressed()
        }

        skipPlayingButton.setOnClickListener {
            displayToast("Skip")
        }

        backPlayingButton.setOnClickListener {
            displayToast("Back")
        }
    }

    //TODO: implement this
    private fun goToSettingsPage() {
//        private val = Intent(this,)
    }

    private fun playPauseButtPressed() {
        if (musicPlaying) {
            playButton.setImageResource(R.drawable.pause_white)
            displayToast("Play")
            musicPlaying = false
        } else {
            playButton.setImageResource(R.drawable.play_arrow_white)
            displayToast("Pause")
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
}
