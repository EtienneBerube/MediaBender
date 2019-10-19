package com.example.mediabender.Integration

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class CommTest() {
    var device: UsbDevice? = null
    var usbConnection: UsbDeviceConnection? = null
    var serial:UsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, usbConnection)

    fun openDevice(){
        serial.open();
        serial.setBaudRate(115200);
        serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serial.setParity(UsbSerialInterface.PARITY_ODD);
        serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        //serial.read(mCallback);
    }

    private val mCallback = UsbSerialInterface.UsbReadCallback {
        // Code here :)
    }


}