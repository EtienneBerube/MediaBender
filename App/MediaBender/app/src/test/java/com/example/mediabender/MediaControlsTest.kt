package com.example.mediabender

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Matchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify

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