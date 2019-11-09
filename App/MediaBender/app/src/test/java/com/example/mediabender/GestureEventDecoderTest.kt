package com.example.mediabender

import android.content.Context
import android.content.SharedPreferences
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Gesture
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Matchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GestureEventDecoderTest {

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    lateinit var gestureEventDecoder: GestureEventDecoder

    @Before
    fun setup(){
        Mockito.`when`<String>(
            mockSharedPreferences!!.getString(
                Matchers.anyString(),
                Matchers.anyString()
            )
        ).thenReturn(null)

        Mockito.`when`<SharedPreferences>(
            mockContext.getSharedPreferences(
                Matchers.anyString(),
                eq(Context.MODE_PRIVATE)
            )
        ).thenReturn(mockSharedPreferences)

        gestureEventDecoder = GestureEventDecoder(mockContext)

    }

    @Test
    fun decodeDefaultLeft(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.LEFT)
        Assert.assertEquals(MediaEventType.PREVIOUS_SONG,result)
    }

    @Test
    fun decodeDefaultRight(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.RIGHT)
        Assert.assertEquals(MediaEventType.SKIP_SONG,result)
    }

    @Test
    fun decodeDefaultUp(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.UP)
        Assert.assertEquals(MediaEventType.RAISE_VOLUME,result)
    }

    @Test
    fun decodeDefaultDown(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.DOWN)
        Assert.assertEquals(MediaEventType.LOWER_VOLUME,result)
    }

    @Test
    fun decodeDefaultNear(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.NEAR)
        Assert.assertEquals(MediaEventType.PLAY,result)
    }

    @Test
    fun decodeDefaultFar(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.FAR)
        Assert.assertEquals(MediaEventType.PAUSE,result)
    }

    @Test
    fun decodeDefaultNone(){
        val result = gestureEventDecoder.gestureToEvent(Gesture.NONE)
        Assert.assertEquals(MediaEventType.NONE,result)
    }

    @Test
    fun decodeDefaultUnknown(){
        var result = gestureEventDecoder.gestureToEvent(Gesture.UNKNOWERROR)
        Assert.assertEquals(MediaEventType.NONE,result)

        result = gestureEventDecoder.gestureToEvent(Gesture.GESTURE1)
        Assert.assertEquals(MediaEventType.NONE,result)

        result = gestureEventDecoder.gestureToEvent(Gesture.GESTURE2)
        Assert.assertEquals(MediaEventType.NONE,result)

        result = gestureEventDecoder.gestureToEvent(Gesture.GESTURE3)
        Assert.assertEquals(MediaEventType.NONE,result)
    }



}