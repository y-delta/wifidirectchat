package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.sendReceive
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.MainActivity.Companion.serverCreated
import com.example.myapp.db.DatabaseUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

class SendReceive(private var socket: Socket?) : Thread() {
    private val inetAddress: InetAddress
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    var decodedMsg: String? = null
    var listening = true
    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (socket != null && listening) {
            try {
                bytes = inputStream!!.read(buffer)
                if (bytes > 0) {
                    Log.d(
                        "MessageReceived",
                        "from " + socket!!.inetAddress.hostAddress
                    )
                    decodedMsg = String(buffer).trim().substring(0, bytes)
                    Log.d("MessageReceived", decodedMsg)
                    Log.d("MessageReceived", "size of string = " + bytes)
                    if (serverCreated) {
                        Log.d(
                            "Forwarding",
                            "Start forwarding messages because I'm the GO"
                        )
                        for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                            if (sendReceiveDevice !== this) {
                                Log.d(
                                    "Forwarding Message",
                                    "from " + socket!!.inetAddress.hostAddress +
                                            " to " + sendReceiveDevice.socket!!.inetAddress
                                        .hostAddress
                                )
                                sendReceiveDevice.write(decodedMsg!!.toByteArray())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (inputStream == null) {
                    Log.d("SendReceive run()", "inputStream is null")
                }
                try {
                    if (!serverCreated) {
                        listening = false
                        socket!!.close()
                    }
                    Log.d(
                        "Socket Closing",
                        "Closing socket due to error IOException"
                    )
                } catch (e2: Exception) {
                    e2.printStackTrace()
                } finally {
                    if (serverCreated) {
                        netAddrSendReceiveHashMap?.remove(inetAddress)
                        Log.d("Socket Closing", "Removed from sendReceiveHashMap")
                        Log.d(
                            "Socket Closing",
                            "items in sendReceiveHashMap = " + netAddrSendReceiveHashMap!!.size
                        )
                        socket = null
                    } else {
                        sendReceive = null
                        socket = null
                    }
                }
            }
        }
    }

    fun write(bytes: ByteArray?) {
        try {
            outputStream!!.write(bytes)
            Log.d("Sending message", String(bytes!!))
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                if (!serverCreated) {
                    listening = false
                    socket!!.close()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            } finally {
                if (serverCreated) netAddrSendReceiveHashMap!!.remove(inetAddress) else {
                    sendReceive = null
                }
            }
        }
    }

    init {
        inetAddress = socket!!.inetAddress
        try {
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
       // lateinit var recievedMsg: String = this.decodedMsg
    }
}