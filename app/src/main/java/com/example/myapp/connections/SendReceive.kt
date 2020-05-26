package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.ipAddrUsernameHashMap
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.MainActivity.Companion.receivedGroupMessage
import com.example.myapp.MainActivity.Companion.serverCreated
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.db.entity.LedgerEntity
import com.example.myapp.ui.groupmessage.GroupMessageFragment
import com.example.myapp.ui.groupmessage.GroupMessageFragment.Companion.appDatabaseCompanion
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
        outloop@while (socket != null && listening && bufferedReader!= null) {
           // var receivedGroupMessage:String
            try {
                //new code
                Log.d("SendReceive", "Reading fresh new message")
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
                        continue@outloop //go back to reading messages
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
                            GroupMessageFragment.mChatListCompanion!!.add(chatEntitySender)
//                    receiverMessageFlag = true
                            var appDatabase = GroupMessageFragment.appDatabaseCompanion
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
                    var messagePass = 0
                    var ledgerEntity = LedgerEntity()
                    loop@ while(true){
                        var debugMessage = ""
                        message = bufferedReader.readLine()
                        sendAlong(message + "\n")
                        when(messagePass){  //date, landmark, location, needs, latitude, longitude, accuracy
                            0 -> {
                                Log.d("ledgerreceive0date", message)
                                ledgerEntity.date = Date(message) // date is added here //entry into db here
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            1 -> {
                                Log.d("ledgerreceive1landmark", message)
                                ledgerEntity.landmark = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            2 -> {
                                Log.d("ledgerreceive2location", message)
                                ledgerEntity.location = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            3 -> {
                                Log.d("ledgerreceive3needs", message)
                                ledgerEntity.needs = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            4 -> {
                                Log.d("ledgerreceive4latitude", message)
                                ledgerEntity.latitude = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            5 -> {
                                Log.d("ledgerreceive5longitude", message)
                                ledgerEntity.longitude = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            6 -> {
                                Log.d("ledgerreceive6accuracy", message)
                                ledgerEntity.accuracy = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            7 -> {
                                Log.d("ledgerreceive7sender", message)
                                ledgerEntity.sender = message
                                messagePass++
                                debugMessage += message + "\n"
                            }
                            8 -> {
                                Log.d("ledgerreceive8", message)
                                if(message.equals(Constants.MESSAGE_TYPE_LEDGER)){
                                    Log.d("LedgerInput", "Attempting to insert item into database")
                                    DatabaseUtil.addNewLedgerToDataBase(appDatabaseCompanion,ledgerEntity)
                                    Log.d("LedgerInput", "Inserted this message to database:-")
                                    Log.d("LedgerInput", debugMessage)
                                    break@loop
                                }
                            }
                        }
                    }
                } else if(message.equals(Constants.REQUEST_TYPE_LEDGER_LIST)){

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