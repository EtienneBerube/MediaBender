package com.example.mediabender

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import com.example.mediabender.service.AlbumCoverFetcher
import java.lang.RuntimeException
import android.content.Intent
import android.content.Intent.getIntent


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

    private val coverFetcher: AlbumCoverFetcher
    private val context = context
    private var track: String = ""
    private var trackID: String = ""
    private var album: String = ""
    private var albumID: String = ""
    private var artist: String = ""
    private var albumArt: Bitmap? = null
    private var playbackState: Boolean = false
    private var player: Int? = null

    private var iF = IntentFilter()
    private val myReceiver = MyReceiver()

    init {
        // google play android actions
        iF.addAction("com.android.music.metachanged")
        iF.addAction("com.android.music.queuechanged")
        iF.addAction("com.android.music.playstatechanged")

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
        iF.addAction("com.spotify.music.playbackstatechanged")

        // registering the broadcast receiver with the intent filter
        registerBroadcastReceiver(iF)

        // setting the current player to INVALID_PLAYER so that it is never null
        setCurrentPlayer("")

        coverFetcher = AlbumCoverFetcher(context)
    }

    // broadcast receiver to set variables to live data on song change
    // TODO: soundcloud behaving weirdly when music player opened but paused, the app doesnt seem to "connect" to it right away
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!wasLaunchedFromRecents(intent)) {
                setCurrentPlayer(intent.action ?: "")
                setPlaybackState(intent)
                setTrack(intent.getStringExtra("track"))
                setAlbum(intent.getStringExtra("album"))
                setArtist(intent.getStringExtra("artist"))
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
                if ((lastAlbum == null && lastArtist == null && lastTrack == null) || lastAlbum != album || lastTrack != track || lastArtist != artist) {
                    lastAlbum = album
                    lastArtist = artist
                    lastTrack = track
                    AlbumCoverFetcher(context).execute(album, artist)
                } else {
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
}
