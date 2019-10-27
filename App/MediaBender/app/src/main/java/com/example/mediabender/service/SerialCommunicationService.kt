package com.example.mediabender.service

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.content.BroadcastReceiver
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.UnsupportedEncodingException
import android.content.IntentFilter
import android.widget.Toast

private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

/**
 * In order to use the service in an activity, one must add:
 *
 * onCreate(){
 * SerialCommunicationService.instance.setService(this)
 * SerialCommunicationService.instance.setDataOnReceiveListener{ runOnUiThread{
 * DEFINE LISTENER HERE. IT CAN BE LEFT BLANK. }}
 * }
 *
 * onResume(){
 * SerialCommunicationService.instance.requestUSBpermission(context)
 * }
 *
 */
class SerialCommunicationService {
    private var device: UsbDevice? = null
    private lateinit var serialPort: UsbSerialDevice
    private lateinit var usbManager: UsbManager
    private var connection: UsbDeviceConnection? = null
    var data: ArrayList<Byte> = ArrayList()
    var dataReceiveListener : (ByteArray) -> Unit = {}

    fun setService(activity: Activity) {
        usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    companion object {
        val instance = SerialCommunicationService()
    }

    /**
     * requestUSBpermission does just that, it requestion a USB permission.
     * It first obtain the device list, for all devices, is our microcontroller here?
     * If yes,
     * filter the intents on our broadcast receiver and link it to the intent receiver.
     * Request a permission from the ubsManager with our pending intent.
     * Since we linked our broadcast receiver, if the permission is accepted,
     * onReceived will be called
     * This function has to be called in activity onResumed since a USBconnection intent is an
     * activity intent and will automatically call a new activity otherwise stated in onResume.
     */
    fun requestUSBpermission(context: Context) {

        val usbDevices = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            appendLog(context,"DEVICE LIST FOUND\n")
            var found = false
            for (entry in usbDevices.entries) {
                device = entry.value
                val deviceVID = entry.value.vendorId
                appendLog(context,"DEVICE ID ${entry.value.vendorId}\n")
                if (deviceVID == 0x2341) {//Arduino Vendor ID
                    appendLog(context,"ARDUINO DEVICE ID FOUND\n")
                    val pi =
                        PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                    val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                    filter.addAction(ACTION_USB_PERMISSION)
                    context.registerReceiver(broadcastReceiver, filter)
                    usbManager.requestPermission(device, pi)
                    found = true
                } else {
                    connection = null
                    device = null
                }

                if (found)
                    break
            }
        } else {
            appendLog(context,"DEVICE LIST NOT FOUND\n")
        }
    }

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
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    if (intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                        appendLog(context, "USB PERMISSION GRANTED\n")
                        openPort(context)
                    } else {
                        appendLog(context, "PERMISSION NOT GRANTED\n")
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    appendLog(context, "USB ATTACHED\n")
                    requestUSBpermission(context)
                }
                else -> {
                    appendLog(context, "USB DETACHED\n")
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
    private fun openPort(context: Context) {
        connection = usbManager.openDevice(device)
        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
        if (serialPort != null) {
            if (serialPort.open()) { //Set Serial Connection Parameters.
                serialPort.setBaudRate(9600)
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
                serialPort.setParity(UsbSerialInterface.PARITY_NONE)
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                serialPort.syncRead(ByteArray(1), 0)
                serialPort.read(dataReceivedCallBack)
                appendLog(context, "Serial Connection Opened!\n")

            } else {
                appendLog(context, "PORT NOT OPEN\n")
            }
        } else {
            appendLog(context, "PORT IS NULL\n")
        }
    }

    /**
     * When we read data, we call a UsbSerialInterface with defined UsbReadCallback function.
     * This function is called whenever we receive information.
     * This is where we interpret the data received.
     * We call dataReceived, passing the data.
     * At the moment, we just take the data into a list.
     */
    private val dataReceivedCallBack = UsbSerialInterface.UsbReadCallback {
        //TODO: implement a received data algorithm/Protocol
        try {
            if (it.isNotEmpty()) {
                dataReceived(it)
                data.addAll(it.asList())
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    /**
     * This function invokes the function variable dataReceiveListener by passing the Byte array.
     */
    private fun dataReceived(it:ByteArray){
        dataReceiveListener.invoke(it)
    }
    /**
     * This function is where the magic happens. We let external Activities define the function of
     * their choice as a our private variable dataReceiveListener that will be called when the data
     * is received. An Activity have to implement this listener, either as an empty function or
     * with an actual algorithm.
     */
    fun setDataOnReceiveListener(feedback : (ByteArray) -> Unit){
        dataReceiveListener = feedback
    }

    /**
     * Creates toast and log of a text
     */
    private fun appendLog(context: Context, text: CharSequence) {
        Log.d("SERIAL", "$text")
        val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    /**
     * Function to close the connection
     * If the serial connection in not closed, there is a risk of memory leak and fatal exception.
     * To be secure, if anything seems wrong, it is required to close the connection.
     */
    private fun closeConnection() {
        serialPort.close()
    }

    /**
     * testUSBConnection returns true or false depending if the Arduino is found.
     */
    fun testUSBConnection():Boolean {
        val usbDevices = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            var found = false
            for (entry in usbDevices.entries) {
                val deviceVID = entry.value.vendorId
                if (deviceVID == 0x2341) {
                    return true
                }
            }
            return false
        } else {
            return false
        }
    }

    /**
     * Function to write to the Arduino. Might have to be implemented later.
     * Never used.
     */
    private fun writeToSerial(string: String) {
        serialPort.write(string.toByteArray())
    }

}