package com.example.mediabender

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.view.KeyEvent

class MediaControls(context: Context) {

    private val context: Context = context

    // audio manager to be able to interact with media players
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun play() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
        )
    }

    fun pause() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        )
    }

    fun next() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        )
    }

    fun previous() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        )
    }

    fun volumeUp() {
        // flag 0 to do nothing
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE,0)
    }

    fun volumeDown() {
        // flag 0 to do nothing
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER,0)
    }
}