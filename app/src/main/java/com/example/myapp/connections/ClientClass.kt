package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.DEVICEMAC
import com.example.myapp.MainActivity.Companion.NETWORK_USERID
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.MainActivity.Companion.NETWORK_USERNAME
import com.example.myapp.MainActivity.Companion.connectedToDeviceAlert
import com.example.myapp.ui.groupmessage.GroupMessageFragment.Companion.appDatabaseCompanion
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
            connectedToDeviceAlert()
            Log.d("ClientClass", "sending username of device to GO ${DEVICEMAC}")
            broadcastMessage("$NETWORK_USERID $NETWORK_USERNAME", Constants.DATA_TYPE_UNIQID_USERNAME)
            // TODO - {resolved} make userid username transfer thread safe
            /*var ledger = appDatabaseCompanion!!.ledgerDao().loadAllChatHistory()
            ledger.observe(
                directMessageActivityCompanion!!,
                androidx.lifecycle.Observer <MutableList<LedgerEntity>?>{
                        ledgerList ->
                    var i = 0
                    while(i < ledgerList!!.size){
                        var ledgerItem = ledgerList[i]!!
                        Log.d("Ledger list items", ledgerItem.needs)
                        var preparedMsg = ""
                        preparedMsg += ledgerItem.date
                        preparedMsg += ledgerItem.landmark
                        preparedMsg += ledgerItem.location
                        preparedMsg += ledgerItem.needs
                        preparedMsg += ledgerItem.latitude
                        preparedMsg += ledgerItem.longitude
                        preparedMsg += ledgerItem.accuracy
                        preparedMsg += NETWORK_USERNAME
                    }
                }
            )*/
            /*var ledgerList = appDatabaseCompanion!!.ledgerDao().loadAllLedgers()
            var i = 0
            while(i < ledgerList!!.size){               //this will be sent to GO
                var ledgerItem = ledgerList[i]!!
//                Log.d("Ledger list items", ledgerItem.needs)
                var preparedMsg = ""
                preparedMsg += ledgerItem.date.toString() + "\n"        //date, landmark, location, needs, latitude, longitude, accuracy
                preparedMsg += ledgerItem.landmark + "\n"
                preparedMsg += ledgerItem.location + "\n"
                preparedMsg += ledgerItem.needs + "\n"
                preparedMsg += ledgerItem.latitude + "\n"
                preparedMsg += ledgerItem.longitude + "\n"
                preparedMsg += ledgerItem.accuracy + "\n"
                preparedMsg += ledgerItem.sender + "\n"
                Log.d("PreparedMessageLedger", preparedMsg)
                broadcastMessage(preparedMsg, Constants.MESSAGE_TYPE_LEDGER)
                i++
            }*/
            BroadcastLedgerList().run()
        } catch (e: IOException) {
            socket = Socket()
            e.printStackTrace()
        }
    }



    init {
        hostAdd = hostAddress.hostAddress
    }
}