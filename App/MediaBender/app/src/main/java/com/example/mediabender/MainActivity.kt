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
import android.content.IntentFilter

private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

class MainActivity : AppCompatActivity() {
    var device: UsbDevice? = null
    private lateinit var serialPort: UsbSerialDevice
    private lateinit var usbManager: UsbManager
    var connection : UsbDeviceConnection? = null
    private lateinit var textView : TextView

    /**
     * Internal private object BroadcastReceiver with overriden function on received.
     * A broadcast receiver is the OS dispatcher of intents.
     * In our case, we need to listen to:
     * ACTION_USB_PERMISSION -> Open the serial port
     * ACTION_USB_DEVICE_ATTACHED -> Start the connection
     * ACTION_USB_DEVICE_DETACHED -> Close the connection
     */
    private val broadcastReceiver = object :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action){
                ACTION_USB_PERMISSION -> {
                    appendLog(textView,"USB PERMISSION ACTION\n")
                    if (intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                        appendLog(textView,"USB PERMISSION GRANTED\n")
                        openPort()
                    } else {
                        appendLog(textView,"PERMISSION NOT GRANTED\n")
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED ->{
                    appendLog(textView,"USB ATTACHED\n")
                    requestUSBpermission()
                }
                else -> {
                    appendLog(textView,"USB DETACHED\n")
                    closeConnection()
                }
            }
        }
    }

    /**
     * Function that opens the port.
     * This function sets the parameters necessary to have a serial connection with Arduino
     * serialPort.read(dataReceivedCallBack) -> When the serial port reads information,
     * it call the function UsbReadCallback from our UsbSerialInterface named dataReceivedCallBack.
     */
    private fun openPort(){
        connection = usbManager.openDevice(device)
        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
        if (serialPort != null) {
            if (serialPort.open()) { //Set Serial Connection Parameters.
                serialPort.setBaudRate(9600)
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
                serialPort.setParity(UsbSerialInterface.PARITY_NONE)
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                serialPort.syncRead(ByteArray(1),0)
                serialPort.read(dataReceivedCallBack)
                appendLog(textView, "Serial Connection Opened!\n")

            } else {
                appendLog(textView,"PORT NOT OPEN\n")
            }
        } else {
            appendLog(textView,"PORT IS NULL\n")
        }
    }

    /**
     * A UsbSerialInterface with defined UsbReadCallback function.
     * This function is called whenever we receive information.
     * This is where we interpret the data received.
     */
    private val dataReceivedCallBack = UsbSerialInterface.UsbReadCallback {
        //TODO: implement a received data algorithm/Protocol
        try {
            if(it.isNotEmpty()) {
                appendLog(textView,
                    "Data received [${it.javaClass}] : ${it.asList().asReversed()}\n")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
    }

    override fun onResume() {
        super.onResume()
        requestUSBpermission()
    }

    /**
     * requestUSBpermission does just that, it requestion a USB permission.
     * It first obtain the device list, for all devices, is our microcontroller here?
     * If yes,
     * filter the intents on our broadcast receiver and link it to the intent receiver.
     * Request a permission from the ubsManager with our pending intent.
     * Since we linked our broadcast receiver, if the permission is accepted,
     * onReceived will be called
     */
    fun requestUSBpermission() {

        val usbDevices = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            appendLog(textView,"DEVICE LIST FOUND\n")
            var found = false
            for (entry in usbDevices.entries) {
                device = entry.value
                val deviceVID = entry.value.vendorId
                appendLog(textView,"DEVICE ID ${entry.value.vendorId}\n")
                if (deviceVID == 0x2341){//Arduino Vendor ID
                    appendLog(textView,"ARDUINO DEVICE ID FOUND\n")
                    val pi = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                    filter.addAction(ACTION_USB_PERMISSION)
                    registerReceiver(broadcastReceiver, filter)
                    usbManager.requestPermission(device, pi)
                    found = true
                } else {
                    connection = null
                    device = null
                }

                if (found)
                    break
            }
        }else{
            appendLog(textView,"DEVICE LIST NOT FOUND\n")
        }
    }

    private fun appendLog(tv: TextView, text: CharSequence) {
        Log.d("SERIAL", "$text")
        runOnUiThread { tv.append(text) }
    }

    /**
     * Function to write to the Arduino. Might have to be implemented later.
     * Never used.
     */
    private fun writeToSerial(string:String) {
        serialPort.write(string.toByteArray())
    }

    /**
     * Function to close the connection
     * If the serial connection in not closed, there is a risk of memory leak and fatal exception.
     * To be secure, if anything seems wrong, it is required to close the connection.
     */
    private fun closeConnection(){
        serialPort.close()
    }

}
