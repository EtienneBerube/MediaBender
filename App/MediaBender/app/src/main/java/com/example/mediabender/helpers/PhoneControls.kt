package com.example.mediabender.helpers

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.mediabender.activities.MainActivity
import com.example.mediabender.models.PhoneEventType


/**
 * This class is used to control the current phone state of the application and handle any related events.
 */

class PhoneControls(context : Context) {


    private val context = context
    var telephony: TelephonyManager
    var telecomManager : TelecomManager
    var inCall: Boolean = false
    private var callState = TelephonyManager.CALL_STATE_IDLE

    init {

        var phoneListener = MyPhoneListener(context)
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    }


    inner class MyPhoneListener(val context: Context) : PhoneStateListener() {


        override fun onCallStateChanged(state: Int, incomingNumber : String){

            when(state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    callState = TelephonyManager.CALL_STATE_IDLE
                    inCall = false
                    //(context as MainActivity).mediaControls.isInCall = false
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    callState = TelephonyManager.CALL_STATE_OFFHOOK
                    inCall = true
                    //(context as MainActivity).mediaControls.isInCall = true

                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    callState = TelephonyManager.CALL_STATE_RINGING
                    inCall = true
                    //(context as MainActivity).mediaControls.isInCall = true

                }
            }

        }

    }

    fun executeEvent(event: PhoneEventType, activity: Activity) {
        when (event) {
            PhoneEventType.RAISE_VOLUME -> (activity as MainActivity).mediaControls.volumeUp()
            PhoneEventType.LOWER_VOLUME -> (activity as MainActivity).mediaControls.volumeDown()
            PhoneEventType.ACCEPT_CALL -> {if (callState != TelephonyManager.CALL_STATE_OFFHOOK) answerCall()}
            PhoneEventType.DECLINE_CALL -> declineCall()
            PhoneEventType.NONE -> {} // do nothing
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