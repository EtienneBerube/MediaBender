package com.example.mediabender

import android.content.Context
import android.telecom.Call
import android.content.Intent
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast

class PhoneControls(val context: Context) {

    var telephony: TelephonyManager
    lateinit var telecomManager : TelecomManager
    lateinit var call : Call

    init {

        var phoneListener = MyPhoneListener(context)
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager


    }

   fun answerCall() {


    }


    fun declineCall() {

        this.call.reject(true, "Cannot Talk. Currently Driving")

    }


    fun disconnectCall() {

        this.call.disconnect()

    }


    inner class MyPhoneListener(val context: Context) : PhoneStateListener() {
        private var phoneRinging = false


        override fun onCallStateChanged(state: Int, incomingNumber : String){

            when(state) {
                TelephonyManager.CALL_STATE_IDLE -> phoneRinging = false
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    phoneRinging = false
                    Toast.makeText(context, "Call Active", Toast.LENGTH_SHORT).show()

                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    phoneRinging = true
                    Toast.makeText(context, "Call Listener", Toast.LENGTH_SHORT).show()
                    answerCall()

                }
            }


        }

    }


}