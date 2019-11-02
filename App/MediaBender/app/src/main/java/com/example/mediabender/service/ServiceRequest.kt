package com.example.mediabender.service

import kotlin.experimental.or

data class ServiceRequest(val request:Request,val sensibility: Sensibility){
    constructor(request:Request): this(request,Sensibility.LOW)
    var toByte:Byte
    init{
        toByte = request.toByte or sensibility.toByte
    }
}

enum class Request(var toByte:Byte){
    FLAG(0x01),
    SENSIBILITY(0x02),
    REQUEST1(0x03),
    REQUEST2(0x04),
    REQUEST3(0x05),
    REQUEST4(0x06),
    REQUEST5(0x07)
}

enum class Sensibility(var toByte:Byte){
    LOW(0x00),
    MEDIUM(0x08),
    HIGH(0x10),
    MAX(0x18)
}