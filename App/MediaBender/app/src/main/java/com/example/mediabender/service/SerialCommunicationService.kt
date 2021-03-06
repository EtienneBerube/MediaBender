package com.example.mediabender.service

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.helpers.MediaControls
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.UnsupportedEncodingException


/**
 * This class is used to read and write to the sensor. It listens to the serial port of the android device
 */
class SerialCommunicationService {
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var device: UsbDevice? = null
    private lateinit var serialPort: UsbSerialDevice
    private lateinit var usbManager: UsbManager
    private var connection: UsbDeviceConnection? = null
    var dataReceiveListener : ((ServiceMessage) -> Unit)? = {}
    var isConnected = false
    var isSystemInitException = false
    var isSensorInitException = false
    var isGestureAvailable = false
    var isAppInBackground = false
    private lateinit var gestureDecoder:GestureEventDecoder
    private lateinit var mediaControls: MediaControls

    fun setService(activity: Activity) {
        usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        activity.applicationContext.registerReceiver(broadcastReceiver, filter)
        gestureDecoder = GestureEventDecoder.getInstance(activity.applicationContext)
        mediaControls = MediaControls(activity.applicationContext)
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
        if(!isConnected) {
            val usbDevices = usbManager.deviceList
            if (usbDevices.isNotEmpty()) {
                var found = false
                for (entry in usbDevices.entries) {
                    device = entry.value
                    val deviceVID = entry.value.vendorId
                    if (deviceVID == 0x2341) {//Arduino Vendor ID
                        val pi =
                            PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                        usbManager.requestPermission(device, pi)
                        found = true
                    } else {
                        connection = null
                        device = null
                    }

                    if (found)
                        break
                }
            }
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
                        if(testUSBConnection()){
                            openPort(context)
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    requestUSBpermission(context)
                }
                else -> {
                    if(isConnected){
                        closeConnection()
                    }
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
                isConnected=true
                serialPort.setBaudRate(9600)
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
                serialPort.setParity(UsbSerialInterface.PARITY_NONE)
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                serialPort.syncRead(ByteArray(1), 0)
                serialPort.read(dataReceivedCallBack)
                sendRequest(ServiceRequest(Request.SENSIBILITY,Sensibility.LOW))//TODO: send the saved sensibility

            }
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
        try {
            if (it.isNotEmpty()) {
                if(isAppInBackground){
                    dataReceiveListener?.invoke(ServiceMessage(it[0]))
                    //val event = gestureDecoder.gestureToMediaEvent(ServiceMessage(it[0]).gesture)
                    //mediaControls.executeEvent(event)
                }else{
                    dataReceived(it)
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    /**
     * This function invokes the function variable dataReceiveListener by passing the Byte array.
     */
    private fun dataReceived(it:ByteArray){
        val message = ServiceMessage(it[0])
        if(message.isRequestAnswer){
            isGestureAvailable = message.isGestureAvailable
            isSystemInitException = message.isSystemInitException
            isSensorInitException = message.isSensorInitException
            //TODO: Exception algorithm
        }else{
            //Null safety addition
            dataReceiveListener?.invoke(message)
        }
    }
    /**
     * This function is where the magic happens. We let external Activities define the function of
     * their choice as a our private variable dataReceiveListener that will be called when the data
     * is received. An Activity have to implement this listener, either as an empty function or
     * with an actual algorithm.
     */
    fun setDataOnReceiveListener(feedback : (ServiceMessage) -> Unit){
        dataReceiveListener = feedback
    }

    fun removeDataOnReceiveListener(){
        dataReceiveListener = null
    }

    /**
     * Function to close the connection
     * If the serial connection in not closed, there is a risk of memory leak and fatal exception.
     * To be secure, if anything seems wrong, it is required to close the connection.
     */
    private fun closeConnection() {
        serialPort.close()
        isConnected=false
    }

    /**
     * testUSBConnection returns true or false depending if the Arduino is found.
     */
    fun testUSBConnection():Boolean {
        if(!isConnected){
            val usbDevices = usbManager.deviceList
            if (usbDevices.isNotEmpty()) {
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
        }else{
            return true
        }
    }

    /**
     * Function to write to the Arduino. ServiceRequest necessary to writte to arduino
     */
    fun sendRequest(request: ServiceRequest) {
        serialPort.write(byteArrayOf(request.toByte))
    }

}