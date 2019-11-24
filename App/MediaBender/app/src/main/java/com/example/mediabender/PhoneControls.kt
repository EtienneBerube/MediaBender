package com.example.mediabender

import android.app.KeyguardManager
import android.content.Context
import android.telecom.Call
import android.content.Intent
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import android.app.NotificationManager
import android.media.AudioManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat.getSystemService



class PhoneControls(val context: Context) {

    var telephony: TelephonyManager
   // var telecomManager : TelecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

   // var keyguardManager : KeyguardManager
    //var audioManager : AudioManager

    init {

        var phoneListener = MyPhoneListener(context)
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)


        //keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
       // audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    }


   /* fun answerCall(context: Context) {
        val buttonUp = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonUp.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK)
        )
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED")
    }
*/
    /*private fun declineCall(context: Context) {

        val buttonDown = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonDown.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK)
        )
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED")
    }*/


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
                    //answerCall(context)

                }
            }


        }

    }


}