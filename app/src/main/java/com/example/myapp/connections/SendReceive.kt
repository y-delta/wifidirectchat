package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.MainActivity.Companion.receivedGroupMessage
import com.example.myapp.MainActivity.Companion.serverCreated
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.utils.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.util.*

class SendReceive(private var socket: Socket?) : Thread() {

    private val inetAddress: InetAddress
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    var listening = true
    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (socket != null && listening) {
           // var receivedGroupMessage:String
            try {
                bytes = inputStream!!.read(buffer)
                if (bytes > 0) {
                    Log.d("MessageReceived", "from " + socket!!.inetAddress.hostAddress)
                    receivedGroupMessage = String(buffer).trim().substring(0, bytes)
                    if(true){ //this is where we would check if the message is ledger fragment or group fragment
                        val chatEntitySender = GroupChatEntity()
                        chatEntitySender.chatType = Constants.MESSAGE_RECEIVER
                        chatEntitySender.chatContent = receivedGroupMessage
                        chatEntitySender.date = Date()
                        chatEntitySender.senderId= "Other device"
                        DirectMessageFragment.mChatListCompanion!!.add(chatEntitySender)
//                    receiverMessageFlag = true
                        var appDatabase = DirectMessageFragment.appDatabaseCompanion
                        DatabaseUtil.addSenderGroupChatToDataBase(appDatabase, chatEntitySender)
                    }
                    Log.d("MessageReceived", receivedGroupMessage)
                    Log.d("MessageReceived", "size of string = $bytes")
                    if (netAddrSendReceiveHashMap?.size!! > 1) {               //this is greater than 1 only when device is either GO or bridge member
                        Log.d("Forwarding", "Start forwarding messages because there are at least 2 sockets open from my device")
                        Log.d("Forwarding", "which means I am either the GO, or bridge member")
                        for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                            if (sendReceiveDevice !== this) {
                                Log.d("Forwarding Message", "from " + socket!!.inetAddress.hostAddress + " to " + sendReceiveDevice.socket!!.inetAddress.hostAddress)
                                sendReceiveDevice.write(receivedGroupMessage.toByteArray())
                               // receivedGroupMessage=receivedGroupMessage
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
                    listening = false
                    socket!!.close()
                    Log.d("Socket Closing", "Closing socket due to error IOException")
                } catch (e2: Exception) {
                    e2.printStackTrace()
                } finally {
                    netAddrSendReceiveHashMap?.remove(inetAddress)
                    Log.d("Socket Closing", "Removed from sendReceiveHashMap")
                    Log.d("Socket Closing", "items in sendReceiveHashMap = " + netAddrSendReceiveHashMap!!.size)
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
                netAddrSendReceiveHashMap!!.remove(inetAddress)     //this used to be only for server but changed when i put every sendreceive in hashmap
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

    companion object{
        fun getMessage(): GroupChatEntity {
            val entry = GroupChatEntity()
            val message: String = receivedGroupMessage
            if (message.isEmpty()) entry.chatContent =
                "no new message" else entry.chatContent =
                message
            return entry
        }

    }
}