package com.example.myapp.ui.directmessage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.ui.activity.ChatListingActivity
import com.example.myapp.ui.activity.ChatMessage
import com.example.myapp.ui.main.ChatAdapter

class DirectMessageFragment : Fragment() {
    var contactList = mutableListOf<ChatMessage>()
    private lateinit var dashboardViewModel: DashboardViewModel

    init {
        contactList.add(ChatMessage("Machan69", "bsdk"))
        contactList.add(ChatMessage("Panda", "kaam kr"))
        contactList.add(ChatMessage("A-Bot", "pls kaam kr"))
        Log.d("DirectMessageFragment", "Init")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_globalmessage, container, false)
        val listView: ListView = root.findViewById(R.id.contactList)
        listView.adapter = ChatAdapter(root.context, R.layout.row, contactList)
        listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
            Toast.makeText(
                root.context,
                "Clicked on" + contactList[position].name,
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(root.context, ChatListingActivity::class.java)
            intent.putExtra("contactName", contactList[position].name)
            startActivityForResult(intent, 6969)
        }

        directMessageActivityCompanion = this.activity

        Log.d("DirectMessageFragment", "onCreateView")
        var mainActivity: MainActivity = context as MainActivity
        mainActivity.testDisplay("Yeno bhadwa rascal")
        return root
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