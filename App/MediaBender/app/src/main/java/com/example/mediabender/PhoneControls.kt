package com.example.mediabender

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService



class PhoneControls(context : Context) {


    private val context = context
    var telephony: TelephonyManager
    var telecomManager : TelecomManager

    val REQUESTCODE = 69



    init {

        var phoneListener = MyPhoneListener(context)
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

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
                    Toast.makeText(context, "Call Ringing", Toast.LENGTH_SHORT).show()

                    answerCall()

                }
            }

        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    fun answerCall() {


        if (checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(context, Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            askPermission()

        }
        telecomManager.acceptRingingCall()

    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun declineCall() {


        if (checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(context, Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            askPermission()

        }
        telecomManager.endCall()



    }

    private fun askPermission() {

        ActivityCompat.requestPermissions(context as MainActivity,
            arrayOf(Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.MODIFY_PHONE_STATE),
            69)
    }


}