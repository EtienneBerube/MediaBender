package com.example.mediabender

import android.content.*
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.example.mediabender.service.AlbumCoverFetcher
import android.content.Intent


open class MetadataHelper(context: Context) {

    // todo make this an inner enum
    // constants to define the current player
    val PLAYER_INVALID = -1       // only if current action unrecognized (*see setPlayer())
    val PLAYER_SPOTIFY = 0
    val PLAYER_GOOGLEPLAY = 1     // *see note below
    // NOTE: any player with android in the action is equivalent to GooglePlay music. An example is
    //       soundcloud, who's intent is the same as the GooglePlay music event, and thus handled the
    //       same way

    private var lastArtist: String? = null
    private var lastAlbum: String? = null
    private var lastTrack: String? = null
    private var lastAlbumArt: Bitmap? = null

    private val context = context
    private var track: String = ""
    private var album: String = ""
    private var artist: String = ""
    private var playbackState: Boolean = false
    private var player: Int? = null

    private var filter = IntentFilter()
    private val myReceiver = MyReceiver()

    init {
        // google play android actions
        filter.addAction("com.android.music.metachanged")
        filter.addAction("com.android.music.queuechanged")
        filter.addAction("com.android.music.playstatechanged")

        // amazon mp3 actions
        filter.addAction("com.amazon.mp3.metachanged")

        // misc. default android player actions
        filter.addAction("com.htc.music.metachanged")
        filter.addAction("fm.last.android.metachanged")
        filter.addAction("com.sec.android.app.music.metachanged")
        filter.addAction("com.nullsoft.winamp.metachanged")
        filter.addAction("com.miui.player.metachanged")
        filter.addAction("com.real.IMP.metachanged")
        filter.addAction("com.sonyericsson.music.metachanged")
        filter.addAction("com.rdio.android.metachanged")
        filter.addAction("com.samsung.sec.android.MusicPlayer.metachanged")
        filter.addAction("com.andrew.apollo.metachanged")

        //Apple Music
        filter.addAction("com.apple.music.playbackstatechanged")
        filter.addAction("com.apple.music.metadatachanged")
        filter.addAction("com.apple.music.queuechanged")

        // spotify actions found on:
        // https://developer.spotify.com/documentation/android/guides/android-media-notifications/
        // IMPORTANT: for Spotify to work, the user must have enabled "Device Broadcast Status"
        filter.addAction("com.spotify.music.metadatachanged")
        filter.addAction("com.spotify.music.queuechanged")
        filter.addAction("com.spotify.music.playbackstatechanged")

        // registering the broadcast receiver with the intent filter
        registerBroadcastReceiver(filter)

        // setting the current player to INVALID_PLAYER so that it is never null
        setCurrentPlayer("")
    }

    // broadcast receiver to set variables to live data on song change
    // TODO: soundcloud behaving weirdly when music player opened but paused, the app doesnt seem to "connect" to it right away
    inner class MyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent){
            if (!wasLaunchedFromRecents(intent)) {
                setCurrentPlayer(intent.action ?: "")
                setPlaybackState(intent)
                setTrack(intent.getStringExtra("track"))
                setAlbum(intent.getStringExtra("album"))
                setArtist(intent.getStringExtra("artist"))

                if (intent.action!!.contains("metadatachanged"))
                    displayAlbumArt()

                with(context as MainActivity) {
                    displayCurrentSong(track, artist)
                    updatePlaybackState(playbackState)

                }
            }
        }

        // if any of the arguments are null, sets the string to blank
        // NOTE: it seems like android has stopped support for fetching album art, song ids and
        //       album ids in an easy way. The song id and album id retrieved from MediaStore dont
        //       correspond to the actual ids, and thus make it impossible to find the album id, and
        //       thus the album art from the album id. There are a few options to get art now:
        //          1. create a MediaSessionManager object using the MEDIA_CONTENT_CONTROL
        //             permission, which is a system permission and only for system apps
        //          2. request tokens from GooglePlay Music, Spotify, etc. which becomes difficult
        //          3. use a third party API that retrieves album art based on song title, artist,
        //             album name, etc. This is also difficult, and would require the user use wifi
        //             to download the album images on-the-fly. Most people probably dont want to
        //             use data for this
        private fun setTrack(_track: String?) {
            track = _track ?: ""
        }

        private fun setAlbum(_album: String?) {
            album = _album ?: ""
        }

        private fun setArtist(_artist: String?) {
            artist = _artist ?: ""
        }

        private fun setPlaybackState(intent: Intent) {
            playbackState = when (player) {
                PLAYER_INVALID -> playbackState
                PLAYER_SPOTIFY -> intent.getBooleanExtra("playstate", playbackState)
                PLAYER_GOOGLEPLAY -> intent.getBooleanExtra("playing", playbackState)
                else -> playbackState
            }
        }

        private fun displayAlbumArt() {
            try {
                if (isNotSameSong()) {
                    lastAlbum = album
                    lastArtist = artist
                    lastTrack = track
                    AlbumCoverFetcher(context, lastAlbumArt).execute(album, artist)
                } else {
                    (context as MainActivity).changeCoverArt(lastAlbumArt)
                    Log.d("Cover Fetcher", "Debounced an HTTP call")
                }
            } catch (e: IllegalStateException) {
                Log.d("Cover Fetcher", "Debounced an HTTP call: Already running")
            }
        }

        protected fun wasLaunchedFromRecents(intent: Intent): Boolean {
            return intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
        }
    }

    fun getTrack(): String {
        return track
    }

    fun getAlbum(): String {
        return album
    }

    fun getArtist(): String {
        return artist
    }

    fun setCurrentPlayer(action: String) {
        if ("android" in action) {
            player = PLAYER_GOOGLEPLAY
        } else if ("spotify" in action) {
            player = PLAYER_SPOTIFY
        } else {    // if intent action unrecognized source, claim invalid player
            player = PLAYER_INVALID
        }
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

    private fun isNotSameSong(): Boolean{
      return (lastAlbum == null && lastArtist == null && lastTrack == null) || lastAlbum != album || lastTrack != track || lastArtist != artist
    }
}