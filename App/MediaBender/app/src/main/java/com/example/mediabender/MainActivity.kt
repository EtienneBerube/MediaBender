package com.example.mediabender

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    // object to allow media control
    private var bs: MediaControls = MediaControls(this);

    private var b_volUp: Button? = null
    private var b_volDown: Button? = null
    private var b_play: Button? = null
    private var b_pause: Button? = null
    private var b_next: Button? = null
    private var b_previous: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeMediaControls()
    }

    private fun initializeMediaControls() {

        // getting buttons
        b_volDown = findViewById<Button>(R.id.b_voldown)
        b_volUp = findViewById<Button>(R.id.b_volup)
        b_play = findViewById<Button>(R.id.b_play)
        b_pause = findViewById<Button>(R.id.b_pause)
        b_next = findViewById<Button>(R.id.b_next)
        b_previous = findViewById<Button>(R.id.b_previous)

        // setting OnClickListeners
        b_volDown?.setOnClickListener {bs?.volumeDown()}
        b_volUp?.setOnClickListener {bs?.volumeUp()}
        b_play?.setOnClickListener {bs?.play()}
        b_pause?.setOnClickListener {bs?.pause()}
        b_next?.setOnClickListener {bs?.next()}
        b_previous?.setOnClickListener {bs?.previous()}
    }

}
