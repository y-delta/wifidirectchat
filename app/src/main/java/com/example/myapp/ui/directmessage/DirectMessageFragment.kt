package com.example.myapp.ui.directmessage
import android.content.Context
import android.database.DataSetObserver
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.delta.chatscreen.ChatArrayAdapter
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.R

data class ChatMessage(var left: Boolean, var message: String)

class DirectMessageFragment : Fragment() {
    private var chatArrayAdapter: ChatArrayAdapter? = null
    private var listView: ListView? = null
    private var chatText: EditText? = null
    private var buttonSend: Button? = null
    private var side = false
    private var globalContext: Context? = null

    init {
        Log.d("DirectMessageFragment", "Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_directmessage, container, false)
        globalContext = this.activity
        super.onCreate(savedInstanceState)
        buttonSend = root.findViewById<View>(R.id.send) as Button
        listView = root.findViewById<View>(R.id.msgview) as ListView
        if (chatArrayAdapter == null) {
            chatArrayAdapter = ChatArrayAdapter(globalContext!!, R.layout.right)

        }
        listView?.adapter = chatArrayAdapter
        chatText = root.findViewById<View>(R.id.msg) as EditText
        chatText!!.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendChatMessage()
            } else false
        }
        buttonSend?.setOnClickListener { sendChatMessage() }
        listView?.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        listView?.adapter = chatArrayAdapter
        //to scroll the list view to bottom on data change
        chatArrayAdapter!!.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                listView!!.setSelection(chatArrayAdapter!!.count - 1)
            }
        })
        return root
    }

    private fun sendChatMessage(): Boolean {
        val broadcastMessage = broadcastMessage(chatText!!.text.toString(), context!!)
        chatArrayAdapter?.add(ChatMessage(side, chatText!!.text.toString()))
        chatText!!.setText("")
        side = !side

        return true
    }


}
