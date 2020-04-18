package com.example.myapp.ui.globalmessage

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class ChatMessage(var name: String, var message: String, var icon: Int = R.drawable.contact)

class ChatActivity : AppCompatActivity() {
    private lateinit var chatText: EditText
    private lateinit var buttonSend: FloatingActionButton

    init {
        Log.d("ChatActivity", "Init")
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        chatText = findViewById(R.id.txtMessage)
        buttonSend = findViewById(R.id.btnSend)
        chatText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                changeButton()
        }
    }

    companion object {
        private const val TAG = "ChatActivity"
    }

    private fun changeButton(): Boolean {
        buttonSend.setImageResource(R.drawable.ic_send)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}