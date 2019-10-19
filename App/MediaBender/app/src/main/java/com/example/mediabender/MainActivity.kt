package com.example.mediabender

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.content.BroadcastReceiver
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import android.widget.TextView
import java.io.UnsupportedEncodingException


private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"


class MainActivity : AppCompatActivity() {
    var device: UsbDevice? = null
    private lateinit var serialPort: UsbSerialDevice
    private lateinit var usbManager: UsbManager
    var connection : UsbDeviceConnection? = null
    private lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        startConnection()
    }

    fun startConnection() {

        val usbDevices = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            Log.d("SERIAL", "DEVICE LIST FOUND")
            var keep = true
            for (entry in usbDevices.entries) {
                device = entry.value
                val deviceVID = entry.value.vendorId
                Log.d("SERIAL", "DEVICE ID $entry.value.vendorId")
                if (deviceVID == 0x2341)
                //Arduino Vendor ID
                {
                    Log.d("SERIAL", "ARDUINO DEVICE ID FOUND")
                    val pi = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    usbManager.requestPermission(device, pi)
                    keep = false
                } else {
                    connection = null
                    device = null
                }

                if (!keep)
                    break
            }
        }
        Log.d("SERIAL", "DEVICE LIST NOT FOUND")
    }

    private val broadcastReceiver = object :
        BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val granted = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    connection = usbManager.openDevice(device)
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600)
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE)
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            serialPort.read(mCallback) //
                            tvAppend(textView as TextView, "Serial Connection Opened!\n")

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN")
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL")
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED")
                }
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                startConnection()
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                startConnection()
            }
        }
    }

    private val mCallback = UsbSerialInterface.UsbReadCallback {
        //TODO: implement a received data algorithm/Protocol
        var data: Int? = null
        try {
            data = it[0].toInt()
            "$data/n"
            tvAppend(textView, data.toString())
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    private fun tvAppend(tv: TextView, text: CharSequence) {
        runOnUiThread { tv.append(text) }
    }

    private fun writeToSerial(string:String) {
        serialPort.write(string.toByteArray())
    }

    private fun closeConnection(){
        serialPort.close()
    }

}
