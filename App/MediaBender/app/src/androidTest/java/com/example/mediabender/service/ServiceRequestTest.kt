package com.example.mediabender.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.experimental.or

@RunWith(AndroidJUnit4::class)
class ServiceRequestTest {
    @Test
    fun constructorTest1() {
        val message1 = ServiceRequest(Request.FLAG)
        val message2 = ServiceRequest(Request.FLAG)
        val message3 = ServiceRequest(Request.SENSIBILITY)
        val message4 = ServiceRequest(Request.SENSIBILITY)
        Assert.assertEquals(message1,message2)
        Assert.assertEquals(message3,message4)
        Assert.assertNotEquals(message1,message3)
        Assert.assertNotEquals(message1,message4)
    }

    @Test
    fun constructorTest2() {
        val message1 = ServiceRequest(Request.SENSIBILITY,Sensibility.MAX)
        val message2 = ServiceRequest(Request.SENSIBILITY,Sensibility.MAX)
        val message3 = ServiceRequest(Request.SENSIBILITY,Sensibility.LOW)
        val message4 = ServiceRequest(Request.SENSIBILITY,Sensibility.LOW)
        Assert.assertEquals(message1,message2)
        Assert.assertEquals(message3,message4)
        Assert.assertNotEquals(message1,message3)
        Assert.assertNotEquals(message1,message4)
    }

    @Test
    fun constructorTest3() {
        val message1 = ServiceRequest(Request.SENSIBILITY,Sensibility.MAX)
        val message2 = ServiceRequest(Request.SENSIBILITY,Sensibility.LOW)
        Assert.assertEquals(message1.toByte,Request.SENSIBILITY.toByte or Sensibility.MAX.toByte)
        Assert.assertEquals(message2.toByte,Request.SENSIBILITY.toByte or Sensibility.LOW.toByte)
    }
}