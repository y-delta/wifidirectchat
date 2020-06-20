package com.example.myapp.connections

import android.util.Log
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.ui.groupmessage.GroupMessageFragment
import com.example.myapp.utils.Constants

class BroadcastLedgerList : Thread() {
    override fun run() {
        Log.d("ServerClass", "attempting to send GO info only to newly connected device")
        var ledgerList = GroupMessageFragment.appDatabaseCompanion!!.ledgerDao().loadAllLedgers()
        var i = 0
        while(i < ledgerList!!.size){               //this will be sent to GO
            var ledgerItem = ledgerList[i]!!
//                Log.d("Ledger list items", ledgerItem.needs)
            var preparedMsg = ""
//            preparedMsg += Constants.MESSAGE_TYPE_LEDGER + "\n"
            preparedMsg += ledgerItem.date.toString() + "\n"        //date, landmark, location, needs, latitude, longitude, accuracy
            preparedMsg += ledgerItem.landmark + "\n"
            preparedMsg += ledgerItem.location + "\n"
            preparedMsg += ledgerItem.needs + "\n"
            preparedMsg += ledgerItem.latitude + "\n"
            preparedMsg += ledgerItem.longitude + "\n"
            preparedMsg += ledgerItem.accuracy + "\n"
            preparedMsg += ledgerItem.sender + "\n"
//            preparedMsg += Constants.MESSAGE_TYPE_LEDGER + "\n"
            Log.d("PreparedMessageLedger", preparedMsg)
            //sendReceive.write(preparedMsg.toByteArray())    //TODO {resolved} problem here - run this on executor (please) or app crash
            broadcastMessage(preparedMsg, Constants.MESSAGE_TYPE_LEDGER)    //thread safe
            i++
        }
    }
}