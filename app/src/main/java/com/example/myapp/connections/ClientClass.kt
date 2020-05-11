package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.sendReceive
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientClass(hostAddress: InetAddress) : Thread() {
    var socket: Socket? = null
    var hostAdd: String
    override fun run() {
        try {
            socket = Socket()
            socket!!.keepAlive = true
            Log.d("Attempting connection to ip", hostAdd.toString())
            socket!!.connect(InetSocketAddress(hostAdd, 2323), 500)
            sendReceive = SendReceive(socket)
            Log.d("ClientClass", "run() sendReceive Object Created")
            sendReceive!!.start()
        } catch (e: IOException) {
            socket = Socket()
            e.printStackTrace()
        }
    }

    init {
        hostAdd = hostAddress.hostAddress
    }
}