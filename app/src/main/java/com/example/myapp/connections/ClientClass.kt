package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.DEVICEMAC
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.utils.Constants
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
            var sendReceive = SendReceive(socket)
            Log.d("ClientClass", "run() sendReceive Object Created")
            MainActivity.netAddrSendReceiveHashMap?.put(socket!!.getInetAddress(), sendReceive!!) //comment this line if the above lines are not commented
            Log.d("SendReceive Size", MainActivity.netAddrSendReceiveHashMap?.size.toString())
            Log.d("ClientClass", "run() added client to sendReceiveHashMap")
            sendReceive!!.start()
            Log.d("ClientClass", "sending MAC ID of device to GO ${DEVICEMAC}")
            broadcastMessage(DEVICEMAC!!, Constants.DATA_TYPE_MAC_ID)
            Log.d("ClientClass", "sending MAC ID of device to GO ${DEVICEMAC}")
        } catch (e: IOException) {
            socket = Socket()
            e.printStackTrace()
        }
    }

    init {
        hostAdd = hostAddress.hostAddress
    }
}