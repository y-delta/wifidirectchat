package com.example.myapp.connections

import android.util.Log
import android.webkit.URLUtil
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.DEVICEMAC
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.MainActivity.Companion.NETWORK_USERNAME
import com.example.myapp.MainActivity.Companion.mainActivityCompanion
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.db.entity.LedgerEntity
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.ui.directmessage.DirectMessageFragment.Companion.directMessageActivityCompanion
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
            Log.d("ClientClass", "sending username of device to GO ${DEVICEMAC}")
            broadcastMessage(NETWORK_USERNAME!!, Constants.DATA_TYPE_MAC_ID)
            Log.d("ClientClass", "sending username of device to GO ${DEVICEMAC}")
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
        } catch (e: IOException) {
            socket = Socket()
            e.printStackTrace()
        }
    }



    init {
        hostAdd = hostAddress.hostAddress
    }
}