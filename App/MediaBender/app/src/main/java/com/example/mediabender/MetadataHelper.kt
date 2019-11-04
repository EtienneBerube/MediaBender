package com.example.mediabender

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import java.io.FileDescriptor

open class MetadataHelper(context: Context) {

    // todo make this an inner enum
    // constants to define the current player
    val PLAYER_INVALID = -1       // only if current action unrecognized (*see setPlayer())
    val PLAYER_SPOTIFY = 0
    val PLAYER_GOOGLEPLAY = 1     // *see note below
// NOTE: any player with android in the action is equivalent to GooglePlay music. An example is
//       soundcloud, who's intent is the same as the GooglePlay music event, and thus handled the
//       same way

    private val context = context
    private var track:String = ""
    private var trackID:String = ""
    private var album:String = ""
    private var albumID:String = ""
    private var artist:String = ""
    private var albumArt: Bitmap? = null
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
    }

    // broadcast receiver to set variables to live data on song change
    // TODO: soundcloud behaving weirdly when music player opened but paused, the app doesnt seem to "connect" to it right away
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setCurrentPlayer(intent.action ?: "")
            setTrack(intent.getStringExtra("track"))
            setAlbum(intent.getStringExtra("album"))
            setArtist(intent.getStringExtra("artist"))

            (context as MainActivity).displayCurrentSong(track,artist,null)
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
        private fun setTrackID(intent: Intent) {

            trackID = when (player) {
                PLAYER_SPOTIFY -> (intent.getStringExtra("id") ?: "").removePrefix("spotify:track:")
                PLAYER_GOOGLEPLAY -> intent.getLongExtra("songId", -1).toString()
                PLAYER_INVALID ->  ""
                else -> ""  // NEVER occurs todo redo with the enum thing
            }
        } // set trackID based on player
        private fun setAlbum(_album: String?) {
            album = _album ?: ""
        }
        private fun setAlbumID(_trackID: String) {
            // creating selection for query
            val selection: String = MediaStore.Audio.Media._ID + " = ?"

            if (player == PLAYER_GOOGLEPLAY) {
                /*val cursor: Cursor? = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ALBUM_ID),
                    selection,
                    arrayOf(_trackID),
                    null
                )

                // if album exists, get the id
                if (cursor?.moveToNext()!!) {
                    albumID =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID))
                }
                cursor.close()*/

            } else albumID = ""
        } // set albumID of given track from trackID
        private fun setArtist(_artist: String?) {
            artist = _artist ?: ""
        }

        // NOT FUNCTIONAL
        private fun setAlbumArt(album_id: Long): Bitmap? {

            // if album_id > -1 then an album exists
            if (album_id > -1) {
                // path uri where album art stored
                val artwork_uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                // uri of current song album art
                val uri: Uri = ContentUris.withAppendedId(artwork_uri, album_id)

                // creating parcel file descriptor to access file ("r" for read only mode)
                val pfd: ParcelFileDescriptor? =
                    context.contentResolver.openFileDescriptor(uri, "r")

                // if pfd exist, then create bitmap from resource
                if (pfd != null) {
                    val fd: FileDescriptor = pfd.fileDescriptor
                    albumArt = BitmapFactory.decodeFileDescriptor(fd)
                }
            }

            // otherwise return null
            return null
        }

        // NOT FUNCTIONAL
        // will need to check if q and do either loadThumbnail method or query
        @SuppressLint("NewApi")
        private fun setAlbumArt2(intent: Intent) {
            val cr: ContentResolver = context.contentResolver

            val cursor1 = cr.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID),
                //MediaStore.Audio.Media.ALBUM + "=?",
                //        MediaStore.Audio.Media.ARTIST + "=?",
                MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                //arrayOf(album,artist),
                null,
                null
            )

            var albumid2: Long = 0

            if (cursor1 != null) {
                while (cursor1.moveToNext()) {
                    val cursorAlbum = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    if ( cursorAlbum == album) {
                        albumid2 = cursor1.getLong(cursor1.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                        break
                    }
                }

            }

            // uri of current song album art
            val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumid2)
            albumArt = cr.loadThumbnail(uri, Size(200,200),null)


            val cursor: Cursor? = cr.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID,MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + "=?",
                arrayOf(albumid2.toString()),
                    null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val path: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            }
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
        if ("android" in  action) {
            player = PLAYER_GOOGLEPLAY
        } else if ("spotify" in  action) {
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
