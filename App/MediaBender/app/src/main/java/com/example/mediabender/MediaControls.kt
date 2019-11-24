package com.example.mediabender

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.view.KeyEvent
import androidx.core.app.ComponentActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.mediabender.dialogs.MediaEventFeedbackDialog
import com.example.mediabender.models.MediaEventType
import java.lang.Exception

class MediaControls(context: Context) {


    private val context: Context = context

    //Whether or not the user is in a voice call
    private var isInCall = false

    // audio manager to be able to interact with media players
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun executeEvent(event: MediaEventType, activity: Activity) {
        executeEvent(event)

        //Shows command
        try {
            val mediaFeedbackDialog = MediaEventFeedbackDialog(event, isMusicPlaying())
            (activity as? FragmentActivity)?.let {
                val fragmentManager = it.supportFragmentManager
                val fragmentTransition = fragmentManager.beginTransaction()

                //Removes if exists
                val prev = fragmentManager.findFragmentByTag("media_feedback")
                if (prev != null) {
                    fragmentTransition.remove(prev)
                }
                fragmentTransition.addToBackStack(null)
                mediaFeedbackDialog.show(fragmentTransition, "media_feedback")
            }
        }catch(e:Exception){
            //TODO: implement a crash algo.
        }
    }

    fun executeEvent(event: MediaEventType) {
        when (event) {
            MediaEventType.RAISE_VOLUME -> volumeUp()
            MediaEventType.LOWER_VOLUME -> volumeDown()
            MediaEventType.SKIP_SONG -> next()
            MediaEventType.PREVIOUS_SONG -> previous()
            MediaEventType.TOGGLE_PLAYSTATE -> togglePlaystate()
            MediaEventType.NONE -> {} // do nothing
        }

    }

    private fun play() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
        )
    }
    private fun pause() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        )
    }
    private fun togglePlaystate() {
        if (audioManager.isMusicActive) pause()
        else play()
    }
    private fun next() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        )
    }
    private fun previous() {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        )
    }
    private fun volumeUp() {
        val source = if (isInCall) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC

        // flag 0 to do nothing
        audioManager.adjustStreamVolume(source, AudioManager.ADJUST_RAISE, 0)
    }
    private fun volumeDown() {

        val source = if (isInCall) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC

        // flag 0 to do nothing
        audioManager.adjustStreamVolume(source, AudioManager.ADJUST_LOWER, 0)
    }

    fun isMusicPlaying(): Boolean {
        return audioManager.isMusicActive
    }
}