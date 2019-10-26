package com.example.mediabender

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.mediabender.service.SerialCommunicationService


class MainActivity : AppCompatActivity() {
    private lateinit var textView : TextView
    private lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        context = this.applicationContext
        SerialCommunicationService.instance.setService(this)
        SerialCommunicationService.instance.setDataOnReceiveListener{
            printData("Data received [${it.javaClass}] : ${it.asList().asReversed()}\n")
        }
    }

    override fun onResume() {
        super.onResume()
        SerialCommunicationService.instance.requestUSBpermission(context)
    }

    fun printData(text:String){
        runOnUiThread{ textView.append("$text")}
    }
}
