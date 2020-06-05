package com.example.myapp.ui.directmessage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.db.AppDatabase
import com.example.myapp.db.entity.ChatEntity
import com.example.myapp.db.entity.LedgerEntity
import com.example.myapp.db.entity.UserEntity
import com.example.myapp.ui.activity.ChatListingActivity
import com.example.myapp.ui.activity.ChatMessage
import com.example.myapp.ui.main.ChatAdapter
import kotlinx.android.synthetic.main.fragment_ledger.*


class DirectMessageFragment : Fragment() {
    var contactList = mutableListOf<ChatMessage>()
    var appDatabase: AppDatabase? = null
    private lateinit var root: View
    private lateinit var listView: ListView

    var mChatList: MutableList<UserEntity>? = ArrayList()
    init {
       // contactList.add(ChatMessage("TestContact","meh","xyz"))
        Log.d("DirectMessageFragment", "Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_globalmessage, container, false)
        listView = root.findViewById(R.id.contactList)
        appDatabase = AppDatabase.getDatabase(activity?.application)
        chatHistory
        //fetchContacts()
//        contactList.clear()
//        for((userid, username) in MainActivity.userIdUserNameHashMap){
//            if(userid != MainActivity.NETWORK_USERID) {
//                contactList.add(
//                    ChatMessage(
//                        username,
//                        "hi",
//                        userid
//                    )
//                )
//                Log.d("Adding to peer list", userid)
//            }
//        }
        //listView.adapter = ChatAdapter(root.context, R.layout.row, contactList)
        listView.emptyView = root.findViewById(R.id.empty)
        listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
            Toast.makeText(
                root.context,
                "Clicked on " + contactList[position].name,
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(root.context, ChatListingActivity::class.java)
            intent.putExtra("contactName", contactList[position].name)
            intent.putExtra("Receiver", contactList[position].receiver)
            startActivityForResult(intent, 6969)
        }
        directMessageActivityCompanion = this.activity

        Log.d("DirectMessageFragment", "onCreateView")
        var mainActivity: MainActivity = context as MainActivity
        mainActivity.testDisplay("Yeno bhadwa rascal")
        return root
    }

    private val chatHistory: Unit
        private get() {
            val chats = appDatabase?.userDao()?.loadAllChatHistory()
            chats?.observe(
                this,
                androidx.lifecycle.Observer <MutableList<UserEntity>>{
                        ledger ->
                        if(ledger!=null) {
                            mChatList = ledger
                            var i = 0
                            while (i < mChatList?.size!!) {
                                if (mChatList!![i].userId != MainActivity.NETWORK_USERID) {
                                    if (!contactList.contains(ChatMessage(mChatList!![i].username,"New Message",mChatList!![i].userId)))
                                        contactList.add(ChatMessage(mChatList!![i].username,"New Message",mChatList!![i].userId))
                                }
                                i++
                            }
                            listView.adapter = ChatAdapter(root.context, R.layout.row, contactList)
                        }
                }
            )
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 6969) {
            if (resultCode == 7070) {

            }
        }
    }

    companion object{
        var directMessageActivityCompanion: LifecycleOwner? = null
        var contactList = mutableListOf<ChatMessage>()

    }
}