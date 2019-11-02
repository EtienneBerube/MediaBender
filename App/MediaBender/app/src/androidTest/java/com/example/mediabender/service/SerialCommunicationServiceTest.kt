package com.example.mediabender.service
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.lang.Exception

/**
 * Instrumented test, which will execute on an Android device_filter.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SerialCommunicationServiceTest {
    @Test
    fun getSerialCommunicationServiceInstance() {
        val serialCommunicationServiceInstance1:SerialCommunicationService = SerialCommunicationService.instance
        val serialCommunicationServiceInstance2:SerialCommunicationService = SerialCommunicationService.instance
        assertEquals(serialCommunicationServiceInstance1,serialCommunicationServiceInstance2)
    }

    @Test
    fun testUSBConnectionException() {
        val instance:SerialCommunicationService = SerialCommunicationService.instance
        var isExceptionCatched=false
        try{
            instance.testUSBConnection()
        }catch (e:Exception){
            isExceptionCatched=true
        }
        assertTrue(isExceptionCatched)
    }

    @Test
    fun listenerTest() {
        val instance:SerialCommunicationService = SerialCommunicationService.instance
        val message = ServiceMessage(0x00)
        var invoked = false
        instance.setDataOnReceiveListener {
            invoked=true
        }
        instance.dataReceiveListener.invoke(message)
        assertTrue(invoked)
    }

}