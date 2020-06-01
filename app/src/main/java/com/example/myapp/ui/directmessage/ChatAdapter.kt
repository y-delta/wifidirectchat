package com.example.myapp.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.myapp.R
import com.example.myapp.db.entity.ChatEntity
import com.example.myapp.db.entity.UserEntity
import com.example.myapp.ui.activity.ChatMessage


class ChatAdapter(var mCtx: Context, var resources: Int, var items: List<ChatMessage>) :
    ArrayAdapter<ChatMessage>(mCtx, resources, items) {
    init {
        //   items = items.reversed()
    }

    private lateinit var mMessagesList: List<UserEntity?>

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resources, null)
        val imageView: ImageView = view.findViewById(R.id.image)
        val titleTextView: TextView = view.findViewById(R.id.textView1)
        val descriptionTextView: TextView = view.findViewById(R.id.textView2)
        var mItem: ChatMessage = items[position]
        imageView.setImageDrawable(mCtx.resources.getDrawable(mItem.icon))
        titleTextView.text = mItem.name
        descriptionTextView.text = mItem.message


        return view
    }

    fun refresh(mMessagesList: List<UserEntity?>) {
        this.mMessagesList = mMessagesList
    }
}