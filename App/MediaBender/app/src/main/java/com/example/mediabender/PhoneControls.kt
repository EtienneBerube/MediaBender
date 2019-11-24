package com.example.mediabender

import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast

class PhoneControls(val context: Context) {

    private var telephony: TelephonyManager

    init {
        // google play android actions
        //iF.addAction("TelephonyManager.ACTION_PHONE_STATE_CHANGED")

        var phoneListener = MyPhoneListener(context)
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

    }


    inner class MyPhoneListener(val context: Context) : PhoneStateListener() {
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

    }


}