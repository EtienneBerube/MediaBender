package com.example.mediabender.service

import kotlin.experimental.and

/**
 * Class that decript the byte message received from the Arduino
 */
data class ServiceMessage(val message: Byte) {
    private val GESTURES:Byte = 0x7F
    private val FLAGS:Byte = 0x7F
    private val NULL:Byte = 0x00
    private val REQUEST:Byte = 0x80.toByte()
    var gesture:Gesture
    var flags:ArrayList<Flags> = ArrayList()
    var isSystemInitException:Boolean
    var isSensorInitException:Boolean
    var isGestureAvailable:Boolean
    var isRequestAnswer:Boolean
    init {
        gesture = when(message and GESTURES){
            Gesture.NONE.toByte->Gesture.NONE
            Gesture.UP.toByte->Gesture.UP
            Gesture.DOWN.toByte->Gesture.DOWN
            Gesture.LEFT.toByte->Gesture.LEFT
            Gesture.RIGHT.toByte->Gesture.RIGHT
            Gesture.NEAR.toByte->Gesture.NEAR
            Gesture.FAR.toByte->Gesture.FAR
            else->Gesture.UNKNOWERROR
        }
        isSystemInitException = ((message and Flags.SYSTEMINITEXCEPTION.toByte) == Flags.SYSTEMINITEXCEPTION.toByte)
        isSensorInitException = ((message and Flags.SENSORINITEXCEPTION.toByte) == Flags.SENSORINITEXCEPTION.toByte)
        isGestureAvailable = ((message and Flags.GESTUREUNAVAILABLE.toByte) == Flags.GESTUREUNAVAILABLE.toByte)
        //Add flags here
        if(message and FLAGS != NULL){
            if(isSystemInitException){
                flags.add(Flags.SYSTEMINITEXCEPTION)
            }
            if(isSensorInitException){
                flags.add(Flags.SENSORINITEXCEPTION)
            }
            if(isGestureAvailable){
                flags.add(Flags.SYSTEMINITEXCEPTION)
            }
            //Add flags here
        }
        isRequestAnswer = (message and REQUEST)==REQUEST
    }
}

enum class Gesture(var toByte:Byte, var toString:String){
    NONE(0x00,"NONE"),
    UP(0x01,"UP"),
    DOWN(0x02,"DOWN"),
    LEFT(0x03,"LEFT"),
    RIGHT(0x04,"RIGHT"),
    NEAR(0x05,"NEAR"),
    FAR(0x06,"FAR"),
    GESTURE1(0x07,"GESTURE1"),
    GESTURE2(0x08,"GESTURE2"),
    GESTURE3(0x09,"GESTURE3"),
    UNKNOWERROR(0xFF.toByte(),"UNKNOWERROR")
    //Add more gestures here
}

enum class Flags(var toByte:Byte, var toString:String){
    SYSTEMINITEXCEPTION(0x01,"SYSTEMINITEXCEPTION"),
    SENSORINITEXCEPTION(0x02,"SENSORINITEXCEPTION"),
    GESTUREUNAVAILABLE(0x04,"GESTUREUNAVAILABLE"),
    FLAG1(0x08,"FLAG1"),
    FLAG2(0x10,"FLAG2"),
    FLAG3(0x20,"FLAG3"),
    FLAG4(0x40,"FLAG4"),
}