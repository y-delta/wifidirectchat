package com.example.myapp.ui.directmessage
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.broadcastMessage
import com.example.myapp.MainActivity.Companion.networkUsername
import com.example.myapp.R
import com.example.myapp.databinding.FragmentDirectmessageBinding
import com.example.myapp.db.AppDatabase
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.ui.adapter.GroupMessageAdapter
import com.example.myapp.utils.AppUtils
import com.example.myapp.utils.Constants
import com.example.myapp.utils.NPALinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class DirectMessageFragment : Fragment() {
    private var chatText: AppCompatEditText? = null
    private var buttonSend: FloatingActionButton? = null
    private var globalContext: Context? = null

    private var recyclerView: RecyclerView? = null
    var adapter: GroupMessageAdapter? = null
    var mChatList: MutableList<GroupChatEntity>? = null
    var binding: FragmentDirectmessageBinding? = null
    var appDatabase: AppDatabase? = null
    private var receiverMessageFlag = false
    private var mObservableChats: LiveData<List<GroupChatEntity>>? = null
    private var layoutManager: NPALinearLayoutManager? = null

    init {
        Log.d("DirectMessageFragment", "Init")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDatabase = AppDatabase.getDatabase(this.activity?.application)
        appDatabaseCompanion = appDatabase
        globalContext = this.activity
//        addReceiverMessage()
    }

    private fun initRecyclerView() {
        layoutManager = NPALinearLayoutManager(globalContext)
        val itemAnimator: SimpleItemAnimator = DefaultItemAnimator()
        itemAnimator.supportsChangeAnimations = false
        recyclerView?.adapter = adapter
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.itemAnimator = itemAnimator
        recyclerView?.setItemViewCacheSize(10)
        recyclerView?.isDrawingCacheEnabled = true
        recyclerView?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        mChatList = ArrayList()
        mChatListCompanion = mChatList
        adapter = GroupMessageAdapter(this.activity, mChatList)
        recyclerView?.adapter = adapter
    }

    private val chatHistory: Unit
        private get() {
            mObservableChats = appDatabase!!.groupChatDao().loadAllChatHistory()
            mObservableChats?.observe(
                this,
                androidx.lifecycle.Observer <MutableList<GroupChatEntity>?>{
                        chatsHistoryList ->
                    if (chatsHistoryList != null) {
                        mChatList = chatsHistoryList
                        adapter!!.refresh(mChatList)
                        adapter!!.notifyItemInserted(chatsHistoryList.size - 1)
                        if (chatsHistoryList.size > 0) layoutManager!!.scrollToPosition(
                            chatsHistoryList.size - 1
                        )
//                        if (receiverMessageFlag) {
//                            addReceiverMessage()
//                        }
                    }
                }
            )
        }

    private fun addSenderMessage() {
        val chatEntitySender = GroupChatEntity()
        chatEntitySender.chatType = Constants.MESSAGE_SENDER
        chatEntitySender.chatContent = chatText?.text.toString()
        chatEntitySender.date = Date()
        chatEntitySender.senderId= "You"
        mChatList!!.add(chatEntitySender)
        receiverMessageFlag = true
        DatabaseUtil.addSenderGroupChatToDataBase(appDatabase, chatEntitySender)
    }
/*
    private fun addReceiverMessage() {
        Handler().postDelayed({
            val chatEntityReceiver = SendReceive.getMessage()
            chatEntityReceiver.chatType = Constants.MESSAGE_RECEIVER
//            chatEntityReceiver.chatContent = SendReceive.getMessage().chatContent
            chatEntityReceiver.date = Date()
            mChatList!!.add(chatEntityReceiver)
            receiverMessageFlag = false
            DatabaseUtil.addReceiverGroupChatToDataBase(appDatabase, chatEntityReceiver)
        }, 1000)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_directmessage, container, false)
        globalContext = this.activity
        super.onCreate(savedInstanceState)

        binding = inflate(inflater, R.layout.fragment_directmessage, container, false)
        appDatabase = AppDatabase.getDatabase(activity?.application)

        recyclerView = root.findViewById(R.id.msgview) as RecyclerView
        buttonSend = root.findViewById<View>(R.id.send) as FloatingActionButton
        chatText = root.findViewById(R.id.msg)

        recyclerView?.adapter = adapter
        buttonSend?.setOnClickListener { sendChatMessage() }

        initRecyclerView()
        chatHistory

        return root
    }

    private fun sendChatMessage(): Boolean {
        val broadcastMessage = broadcastMessage(chatText!!.text.toString(), requireContext())
        if(chatText?.text.toString().trim().isNotEmpty()) {
            addSenderMessage()
            // clear edit text
            chatText?.setText("")
        } else {
            AppUtils.toastMessage(this.activity, "Please enter some message")
        }

        return true
    }

    companion object{
        var appDatabaseCompanion:AppDatabase? = null
        var mChatListCompanion: MutableList<GroupChatEntity>? = null
    }
}