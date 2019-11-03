package com.example.mediabender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.widget.Toast

open class MetadataHelper(context: Context) {

    private val context = context
    private var live_track:String = ""
    private var live_album:String = ""
    private var live_artist:String = ""
    private var live_uri:String = ""

    private var iF = IntentFilter()
    private val myReceiver = MyReceiver()

    init {
        // google play android actions
        iF.addAction("com.android.music.metachanged")

        // amazon mp3 actions
        iF.addAction("com.amazon.mp3.metachanged")

        // misc. default android player actions
        iF.addAction("com.htc.music.metachanged")
        iF.addAction("fm.last.android.metachanged")
        iF.addAction("com.sec.android.app.music.metachanged")
        iF.addAction("com.nullsoft.winamp.metachanged")
        iF.addAction("com.miui.player.metachanged")
        iF.addAction("com.real.IMP.metachanged")
        iF.addAction("com.sonyericsson.music.metachanged")
        iF.addAction("com.rdio.android.metachanged")
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged")
        iF.addAction("com.andrew.apollo.metachanged")

        // spotify actions found on:
        // https://developer.spotify.com/documentation/android/guides/android-media-notifications/
        // IMPORTANT: for Spotify to work, the user must have enabled "Device Broadcast Status"
        iF.addAction("com.spotify.music.metadatachanged")
        iF.addAction("com.spotify.music.queuechanged")

        // registering the broadcast receiver with the intent filter
        registerBroadcastReceiver(iF)
    }

    // broadcast receiver to set variables to live data on song change
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setTrack(intent.getStringExtra("track"))
            setAlbum(intent.getStringExtra("album"))
            setArtist(intent.getStringExtra("artist"))
            setURI(intent.toUri(Intent.URI_INTENT_SCHEME))
        }
        // if any of the arguments are null, sets the string to blank
        fun setTrack(track: String?) {
            live_track = track ?: ""
            toast(live_track)
        }
        fun setAlbum(album: String?) {
            live_album = album ?: ""
            toast(live_album)
        }
        fun setArtist(artist: String?) {
            live_artist = artist ?: ""
            toast(live_artist)
        }
        fun setURI(uri: String?) {
            live_uri = uri ?: ""
            toast(live_uri)
        }
    }

    fun getTrack(): String {
        return live_track
    }
    fun getAlbum(): String {
        return live_album
    }
    fun getArtist(): String {
        return live_artist
    }
    fun getURI(): String {
        return live_uri
    }

    fun registerBroadcastReceiver(intentFilter: IntentFilter) {
        context.registerReceiver(myReceiver, intentFilter)
    }

    // unregister the receiver from the context
    // must be called in the onDestroy of the activity MetadataHelper is a member of
    fun unregisterBroadcastReceiver() {
        context.unregisterReceiver(myReceiver)
    }

    // only for testing purposes
    fun getBroadcastReceiverForTesting(): BroadcastReceiver {
        return myReceiver
    }

    // protected open so that the test can override it, since there is no activity to toast to
    protected open fun toast(str: String) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }
}
