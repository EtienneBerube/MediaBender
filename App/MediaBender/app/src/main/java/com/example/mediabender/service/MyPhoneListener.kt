package com.example.mediabender.service

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

/*class MyPhoneListener(val context: Context) : PhoneStateListener() {
    var phoneRinging = false

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
            }
        }

    }





}*/


