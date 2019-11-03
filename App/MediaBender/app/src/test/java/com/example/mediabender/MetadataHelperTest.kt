package com.example.mediabender

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*

class MetadataHelperTest {

    @Mock
    private var context = mock(Context::class.java)

    private var mh = MetadataHelperNoToast(context)

    // making sure that the receiver is registered when the MetadataHelper is created
    @Test
    fun testRegisterReceiver() {
        verify(context, atLeastOnce()).registerReceiver(any(),any())
    }

/* Not functional at the moment issue with initializing the intent, the extras are always blank
    @Test
    fun testBroadcastReceiver() {
        val track = "track_abc"
        val album = "album_123"
        val artist = "artist_doremi"
        val uri = "uri_bbunme"

        val intent = Intent("DUMMY_ACTION")
        intent.putExtra("track",track)
        intent.putExtra("album",album)
        intent.putExtra("artist",artist)
        intent.putExtra("uri",uri)

        //
        val br = mh.getBroadcastReceiverForTesting()
        br.onReceive(context, intent)

        assertEquals(track,mh.getTrack())
        assertEquals(album,mh.getAlbum())
        assertEquals(artist,mh.getArtist())
        assertEquals(uri,mh.getURI())
    }
*/


    inner class MetadataHelperNoToast(context: Context) : MetadataHelper(context) {
        override fun toast(str: String) {}  // doing nothing because no activity to toast
    }

}