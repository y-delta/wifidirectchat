package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.connectedToDeviceAlert
import com.example.myapp.MainActivity.Companion.liveConnectedDevice
import com.example.myapp.MainActivity.Companion.userIdUserNameHashMap
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.ui.groupmessage.GroupMessageFragment
import com.example.myapp.utils.Constants
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ServerClass : Thread() {
    var socket: Socket? = null
    var serverSocket: ServerSocket? = null

    override fun run() {
        try {
            serverSocket = ServerSocket(2323)
            serverSocket!!.reuseAddress = true
            while (true) {
                Log.d("ServerClass", "run() listening to connections")
                socket = serverSocket!!.accept()
                socket!!.setKeepAlive(true)
                Log.d(
                    "ServerClass",
                    "run() accepted connection from " + socket!!.getInetAddress().hostName
                )
                var sendReceive = SendReceive(socket)
                netAddrSendReceiveHashMap?.put(socket!!.getInetAddress(), sendReceive!!) //comment this line if the above lines are not commented
                Log.d("SendReceive Size", netAddrSendReceiveHashMap?.size.toString())
                Log.d("ServerClass", "run() added client to sendReceiveHashMap")
                sendReceive!!.start()
                // TODO {resolved} run the below code on a separate thread
                BroadcastLedgerList().run()

            }
        } catch (se: IOException) {
            se.printStackTrace()
            try {
//                userIdUserNameHashMap?.remove(socket!!.inetAddress.hostAddress)
                netAddrSendReceiveHashMap?.remove(socket!!.inetAddress)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        netAddrSendReceiveHashMap = ConcurrentHashMap<InetAddress, SendReceive>()
    }
}