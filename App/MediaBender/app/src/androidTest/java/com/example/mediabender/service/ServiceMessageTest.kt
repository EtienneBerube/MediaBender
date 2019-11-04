package com.example.mediabender.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServiceMessageTest {
    @Test
    fun constructorTest1() {
        val message1 = ServiceMessage(0x00)
        val message2 = ServiceMessage(0x07)
        Assert.assertNotEquals(message1,message2)
    }

    @Test
    fun constructorTest2() {
        val message1 = ServiceMessage(0x06)
        val message2 = ServiceMessage(0x06)
        Assert.assertEquals(message1,message2)
    }

    @Test
    fun constructorTest3() {
        val message1 = ServiceMessage(0x7F)
        val message2 = ServiceMessage(0x80.toByte())
        message1.isGestureAvailable
        Assert.assertFalse( message1.isRequestAnswer)
        Assert.assertTrue( message2.isRequestAnswer)
    }
}