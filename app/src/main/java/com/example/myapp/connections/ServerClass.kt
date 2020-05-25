package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.ipAddrUsernameHashMap
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
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
                //                    sendReceiveArrayList.add(sendReceive);
//                    if(!sendReceiveHashMap.containsKey(socket.getInetAddress())) {
//                        sendReceiveHashMap.put(socket.getInetAddress(), sendReceive);
//                    }
//                    else{
//                        try{
//                            socket.close();
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        finally {
//                            sendReceiveHashMap.remove(socket.getInetAddress());
//                            sendReceiveHashMap.put(socket.getInetAddress(), sendReceive);
//                        }
//
//                    }
                netAddrSendReceiveHashMap?.put(socket!!.getInetAddress(), sendReceive!!) //comment this line if the above lines are not commented
                Log.d("SendReceive Size", netAddrSendReceiveHashMap?.size.toString())
                Log.d("ServerClass", "run() added client to sendReceiveHashMap")
                sendReceive!!.start()

            }
        } catch (se: IOException) {
            se.printStackTrace()
            try {
                ipAddrUsernameHashMap?.remove(socket!!.inetAddress.hostAddress)
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