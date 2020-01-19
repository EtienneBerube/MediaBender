package com.example.mediabender

import android.content.Context
import com.example.mediabender.helpers.MediaControls
import org.mockito.Mock
import org.mockito.Mockito

class MediaControlsTest {

    @Mock
    private var context = Mockito.mock(Context::class.java)

    private val mc = MediaControls(context)

    /* TODO
    // making sure that the audioManager is created when the MediaControls is created
    @Test
    fun testAudioManagerInitialization() {
        verify(context, atLeastOnce()).getSystemService("DUMMY")
    }
    */
}