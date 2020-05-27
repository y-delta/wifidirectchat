package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.ipAddrUsernameHashMap
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

                Log.d("ServerClass", "attempting to send GO info only to newly connected device")
                var ledgerList = GroupMessageFragment.appDatabaseCompanion!!.ledgerDao().loadAllLedgers()
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
                    sendReceive.write(preparedMsg.toByteArray())
                    i++
                }

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