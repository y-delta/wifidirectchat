package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.ipAddrUsernameHashMap
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.MainActivity.Companion.receivedGroupMessage
import com.example.myapp.MainActivity.Companion.serverCreated
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.utils.Constants
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.util.*

class SendReceive(private var socket: Socket?) : Thread() {

    private val inetAddress: InetAddress
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var inputStreamReader: InputStreamReader? = null
    lateinit var bufferedReader: BufferedReader
    lateinit var message :String
    var listening = true
    var messageStarted = true
    var messageStartedType = ""
    val chatEntitySender = GroupChatEntity()

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (socket != null && listening && bufferedReader!= null) {
           // var receivedGroupMessage:String
            try {
                //new code
                message = bufferedReader.readLine()

                //this message is not sent along if it is IP addr because this transmission is only from GM/Bridge to GO
                if(message == Constants.DATA_TYPE_MAC_ID){  // this happens only for the first time that's why this message is not sendAlong
                    message = bufferedReader.readLine()
                    Log.d("Username", message)
                    ipAddrUsernameHashMap.put(this.inetAddress.hostAddress, message)
                    Log.d("ipAddrUsernameHashMap", "${this.inetAddress.hostAddress}")
                    Log.d("ipAddrUsernameHashMap", "size = ${ipAddrUsernameHashMap.size}")
                    message = bufferedReader.readLine()
                    if(message == Constants.DATA_TYPE_MAC_ID){
                        Log.d("Username", "done reading MAC ID")
                        continue //go back to reading messages
                    }
                }
                sendAlong(message + "\n")
                Log.d("MessageReceived", message)
                Log.d("MessageReceived", "size of string = ${message.length}")
                if(message.equals(Constants.MESSAGE_TYPE_GROUP)){
                    var pass = 1
                    while(true){    //run this indefinitely until you encounter the ending frame
                        message = bufferedReader.readLine()
                        Log.d("MessageReceived", message)
                        Log.d("MessageReceived", "size of string = ${message.length}")
                        sendAlong(message + "\n")
                        if(message.equals(Constants.MESSAGE_TYPE_GROUP)){       //if you encounter this, the frame is ending
                            DirectMessageFragment.mChatListCompanion!!.add(chatEntitySender)
//                    receiverMessageFlag = true
                            var appDatabase = DirectMessageFragment.appDatabaseCompanion
                            DatabaseUtil.addSenderGroupChatToDataBase(appDatabase, chatEntitySender)
                            break
                        }
                        else{
                            if(pass == 1){
                                pass++
                                chatEntitySender.chatType = Constants.MESSAGE_RECEIVER
                                chatEntitySender.chatContent = ""
                                chatEntitySender.date = Date()
                                chatEntitySender.senderId = message
                            }
                            else{
                                pass++
                                chatEntitySender.chatContent += "\n" + message
                                Log.d("group message", "senderID = ${chatEntitySender.senderId}")
                            }
                        }
                    }
                } else if(message.equals(Constants.MESSAGE_TYPE_LEDGER)){

                }


                /*bytes = message.length
                if(bytes > 0){
                    if(message.equals(Constants.MESSAGE_TYPE_GROUP)){

                    }
                }
                if (bytes > 0) {
                    Log.d("MessageReceived", "from " + socket!!.inetAddress.hostAddress)
//                    receivedGroupMessage = String(buffer).trim().substring(0, bytes)
                    receivedGroupMessage = message
                    if(true){ //this is where we would check if the message is ledger fragment or group fragment
                        val chatEntitySender = GroupChatEntity()
                        chatEntitySender.chatType = Constants.MESSAGE_RECEIVER
                        chatEntitySender.chatContent = receivedGroupMessage
                        chatEntitySender.date = Date()
                        chatEntitySender.senderId= "Other device"
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
                }*/
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
                    ipAddrUsernameHashMap?.remove(inetAddress.hostAddress)
                    netAddrSendReceiveHashMap?.remove(inetAddress)
                    Log.d("Socket Closing", "Removed from sendReceiveHashMap")
                    Log.d("Socket Closing", "items in sendReceiveHashMap = " + netAddrSendReceiveHashMap!!.size)
                }
            }
        }
    }

    fun sendAlong(msg:String){      //this function will forward the msg to every other node it is connected to on the network
        if (netAddrSendReceiveHashMap?.size!! > 1) {               //this is greater than 1 only when device is either GO or bridge member
            Log.d("Forwarding", "Start forwarding messages because there are at least 2 sockets open from my device")
            Log.d("Forwarding", "which means I am either the GO, or bridge member")
            for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                if (sendReceiveDevice !== this) {
                    Log.d("Forwarding Message", "from " + socket!!.inetAddress.hostAddress + " to " + sendReceiveDevice.socket!!.inetAddress.hostAddress)
                    sendReceiveDevice.write(msg.toByteArray())
                    // receivedGroupMessage=receivedGroupMessage
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
            inputStreamReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
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