package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.MAIN_EXECUTOR
import com.example.myapp.MainActivity.Companion.NETWORK_USERID
import com.example.myapp.MainActivity.Companion.NETWORK_USERNAME
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.MainActivity.Companion.userIdUserNameHashMap
import com.example.myapp.MainActivity.Companion.netAddrSendReceiveHashMap
import com.example.myapp.MainActivity.Companion.receivedGroupMessage
import com.example.myapp.MainActivity.Companion.serverCreated
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.ChatEntity
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.db.entity.LedgerEntity
import com.example.myapp.db.entity.UserEntity
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
    lateinit var USERID: String
    var listening = true
    var messageStarted = true
    var messageStartedType = ""
    val chatEntitySender = GroupChatEntity()
    private val dmEntity = ChatEntity()
    private val userEntity = UserEntity()

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        outloop@while (socket != null && listening && bufferedReader!= null) {
           // var receivedGroupMessage:String
            try {
                //new code
                var sendString = ""
                Log.d("SendReceive", "Reading fresh new message")
                message = bufferedReader.readLine()

                //this message is not sent along if it is IP addr because this transmission is only from GM/Bridge to GO
                if(message == Constants.DATA_TYPE_UNIQID_USERNAME){  // this happens only for the first time that's why this message is not sendAlong
                    message = bufferedReader.readLine()
                    Log.d("UserId Username", message)
                    var userid_username = message.split(" ")
                    userIdUserNameHashMap.put(userid_username[0], userid_username[1])
                    this.USERID = userid_username[0]
                    Log.d("useridUsernameHashMap", "size = ${userIdUserNameHashMap.size}")
                    message = bufferedReader.readLine()
                    if(message == Constants.DATA_TYPE_UNIQID_USERNAME){
                        Log.d("Username", "done reading MAC ID")
                        continue@outloop //go back to reading messages
                    }
                } else if(message.equals(Constants.REQUEST_TYPE_LEDGER_LIST)){
                    Log.d("REQUESTLEDGER", "Request received $message")
                    do{
                        message = bufferedReader.readLine()
                        Log.d("REQUESTLEDGER", "$message")
                    }while(!message.equals(Constants.REQUEST_TYPE_LEDGER_LIST))
                    Log.d("REQUESTLEDGER", "$message")
                    var ledgerList = appDatabaseCompanion!!.ledgerDao().loadAllLedgers()
                    var i = 0
                    while(i < ledgerList!!.size){               //this will be sent to GO
                        var ledgerItem = ledgerList[i]!!
//                Log.d("Ledger list items", ledgerItem.needs)
                        var preparedMsg = ""
                        preparedMsg += Constants.MESSAGE_TYPE_LEDGER + "\n"
                        preparedMsg += ledgerItem.date.toString() + "\n"        //date, landmark, location, needs, latitude, longitude, accuracy
                        preparedMsg += ledgerItem.landmark + "\n"
                        preparedMsg += ledgerItem.location + "\n"
                        preparedMsg += ledgerItem.needs + "\n"
                        preparedMsg += ledgerItem.latitude + "\n"
                        preparedMsg += ledgerItem.longitude + "\n"
                        preparedMsg += ledgerItem.accuracy + "\n"
                        preparedMsg += MainActivity.NETWORK_USERNAME + "\n"
                        preparedMsg += Constants.MESSAGE_TYPE_LEDGER + "\n"
                        Log.d("PreparedMessageLedger", preparedMsg)
                        this.write(preparedMsg.toByteArray())
                        i++
                    }
                    continue@outloop
                }

//                sendAlong(message + "\n")
                sendString += message + "\n"
                Log.d("MessageReceived", message)
                Log.d("MessageReceived", "size of string = ${message.length}")
                if(message.equals(Constants.MESSAGE_TYPE_GROUP)){
                    var pass = 1
                    while(true){    //run this indefinitely until you encounter the ending frame
                        message = bufferedReader.readLine()
                        Log.d("MessageReceived", message)
                        Log.d("MessageReceived", "size of string = ${message.length}")
//                        sendAlong(message + "\n")
                        sendString += message + "\n"
                        if(message.equals(Constants.MESSAGE_TYPE_GROUP)){       //if you encounter this, the frame is ending
//                            GroupMessageFragment.mChatListCompanion!!.add(chatEntitySender)
//                    receiverMessageFlag = true
                            var appDatabase = GroupMessageFragment.appDatabaseCompanion
                            DatabaseUtil.addSenderGroupChatToDataBase(appDatabase, chatEntitySender)
                            sendAlong(sendString)
                            sendString = ""
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
//                        sendAlong(message + "\n")
                        sendString += message + "\n"
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
                                    sendAlong(sendString)
                                    sendString = ""
                                    break@loop
                                }
                            }
                        }
                    }
                } else if(message.equals(Constants.MESSAGE_TYPE_UNIQID_USERNAME)){
                    var userid = ArrayList<String>()
                    while(true) {
                        message = bufferedReader.readLine()
                        if(message.equals(Constants.MESSAGE_TYPE_UNIQID_USERNAME)){
                            sendAlong(sendString)
                            sendString = ""

                            if(!serverCreated && netAddrSendReceiveHashMap!!.size == 1) {            // this runs only if device is not GO or Bridge GM
                                //this removes entries that were previously in the network but have since left
                                for ((uid, _) in userIdUserNameHashMap) {
                                    if(!userid.contains(uid)){
                                        userIdUserNameHashMap.remove(uid)
                                    }
                                }
                            }
                            //TODO This is where we will insert all the data from hashmap into the userid-username database
                            userEntity.userId = NETWORK_USERID
                            userEntity.username = NETWORK_USERNAME
                            DatabaseUtil.addUserToDataBase(appDatabaseCompanion,userEntity)
                            break
                        }
                        sendString += message + "\n"
                        Log.d("UserID Username", message)
                        var userid_username = message.split(" ")
                        if(userid_username.size == 2 && userid_username[0] != NETWORK_USERNAME) {
                            userIdUserNameHashMap.put(userid_username[0], userid_username[1])
                            userid.add(userid_username[0])
                        }
                        Log.d("useridUsernameHashMap", "size = ${userIdUserNameHashMap.size}")
                        sendAlong(sendString)
                        sendString = ""
                    }

                } else if(message.equals(Constants.MESSAGE_TYPE_DIRECT)){ //recipientid, networkuserid, messageid, date, msg
                    message = bufferedReader.readLine() //recipientid
                    sendString += message + "\n"
                    if(!message.equals(NETWORK_USERID)){
                        while(true){
                            message = bufferedReader.readLine()
                            sendString += message + "\n"
                            if(message.equals(Constants.MESSAGE_TYPE_DIRECT)){
                                sendAlong(sendString)
                                sendString = ""
                                break
                            }
                        }
                    } else{
                        message = bufferedReader.readLine() //networkuserid
                        var messageSenderId = message
                        message = bufferedReader.readLine() //messageid
                        var messageId = message.toInt()
                        var messageString = ""
                        message = bufferedReader.readLine() //date
                        var messageDate = Date(message)
                        while(true){
                            message = bufferedReader.readLine() //msg
                            messageString += message + "\n"
                            if(message.equals(Constants.MESSAGE_TYPE_DIRECT)){
                                Log.d("DirectMessageReceived", "$messageSenderId says $messageString at $messageDate")
                                // TODO insert message to Database
//                                dmEntity.date = messageDate
//                                dmEntity.chatContent = messageString
//                                dmEntity.sender = messageSenderId
//                                dmEntity.chatType = Constants.MESSAGE_RECEIVER
//                                dmEntity.receiver = NETWORK_USERID
//                                DatabaseUtil.addReceiverChatToDataBase(appDatabaseCompanion, dmEntity)

                                // TODO send messageReceived response back to messageSenderId in the form of broadcastMessage
                                broadcastMessage("$messageSenderId\n$messageId", Constants.RESPONSE_TYPE_DIRECT)
                                sendString = ""
                                break
                            }
                        }
                    }
                } else if(message == Constants.RESPONSE_TYPE_DIRECT){ //messagereceiverid, messageid
                    message = bufferedReader.readLine() //recipientid
                    sendString += message + "\n"
                    if(!message.equals(NETWORK_USERID)){
                        while(true){
                            message = bufferedReader.readLine()
                            sendString += message + "\n"
                            if(message.equals(Constants.RESPONSE_TYPE_DIRECT)){
                                sendAlong(sendString)
                                sendString = ""
                                break
                            }
                        }
                    } else{
                        message = bufferedReader.readLine() //messageid
                        var messageId = message
                        // TODO change in db, attribute received to true where record = messageId
                        message = bufferedReader.readLine()
                        if(message == Constants.RESPONSE_TYPE_DIRECT){

                        } else{
                            // lol unreachable code XDXD trolled u dev
                        }
                    }
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
                    userIdUserNameHashMap?.remove(USERID)
                    netAddrSendReceiveHashMap?.remove(inetAddress)
                    Log.d("Socket Closing", "Removed from sendReceiveHashMap")
                    Log.d("Socket Closing", "items in sendReceiveHashMap = " + netAddrSendReceiveHashMap!!.size)
                }
            }
        }
    }

    fun sendAlong(msg:String){      //this function will forward the msg to every other node it is connected to on the network
        /*if (netAddrSendReceiveHashMap?.size!! > 1) {               //this is greater than 1 only when device is either GO or bridge member
            Log.d("Forwarding", "Start forwarding messages because there are at least 2 sockets open from my device")
            Log.d("Forwarding", "which means I am either the GO, or bridge member")
            for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                if (sendReceiveDevice !== this) {
                    Log.d("Forwarding Message", "from " + socket!!.inetAddress.hostAddress + " to " + sendReceiveDevice.socket!!.inetAddress.hostAddress)
                    sendReceiveDevice.write(msg.toByteArray())
                    // receivedGroupMessage=receivedGroupMessage
                }
            }
        }*/
        if(MAIN_EXECUTOR!=null)
            MAIN_EXECUTOR!!.execute(SendAlongRunnable(msg, this))
    }

    class SendAlongRunnable(msg:String, sendReceive: SendReceive) : Runnable{

        var msg = msg
        var sendReceive = sendReceive

        override fun run() {
            if (netAddrSendReceiveHashMap?.size!! > 1) {               //this is greater than 1 only when device is either GO or bridge member
                Log.d("Forwarding", "Start forwarding messages because there are at least 2 sockets open from my device")
                Log.d("Forwarding", "which means I am either the GO, or bridge member")
                for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                    if (sendReceiveDevice !== sendReceive) {
                        Log.d("Forwarding Message", "from " + sendReceive.socket!!.inetAddress.hostAddress + " to " + sendReceiveDevice.socket!!.inetAddress.hostAddress)
                        sendReceiveDevice.write(msg.toByteArray())
                        // receivedGroupMessage=receivedGroupMessage
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
        fun getMessage(): ChatEntity {
            val entry = ChatEntity()
            val message: String = receivedGroupMessage
            entry.chatContent = message
            entry.chatType = Constants.MESSAGE_RECEIVER
            return entry
        }

    }
}