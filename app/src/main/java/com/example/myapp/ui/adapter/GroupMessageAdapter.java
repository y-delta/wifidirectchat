package com.example.myapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.databinding.MessageViewReceiverBinding;
import com.example.myapp.databinding.MessageViewSenderBinding;
import com.example.myapp.db.entity.ChatEntity;
import com.example.myapp.db.entity.GroupChatEntity;
import com.example.myapp.utils.Constants;
import com.example.myapp.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

private LayoutInflater mLayoutInflater;
private List<GroupChatEntity> mMessagesList;
        Context activity;
public final int VIEW_RECEIVER_MESSAGE = 1, VIEW_SENDER_MESSAGE = 2;
        int lastPosition = 0;

public GroupMessageAdapter(Context context, List<GroupChatEntity> objects) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMessagesList = objects;
        activity = context;
        }

public void refresh(List<GroupChatEntity> mMessagesList) {
        this.mMessagesList = mMessagesList;
        }

@NotNull
@Override
public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

        case VIEW_RECEIVER_MESSAGE:
        MessageViewSenderBinding binding1 = DataBindingUtil.inflate(mLayoutInflater, R.layout.message_view_sender, parent, false);
        return new MessageRightViewHolder(binding1);

        case VIEW_SENDER_MESSAGE:
        MessageViewReceiverBinding binding2 = DataBindingUtil.inflate(mLayoutInflater, R.layout.message_view_receiver, parent, false);
        return new MessageLeftViewHolder(binding2);
        }

        return null;
        }

@Override
public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MessageRightViewHolder) {

final GroupChatEntity message = mMessagesList.get(position);
        ((MessageRightViewHolder) holder).binding.textviewMessageSender.setText(message.getChatContent());
        ((MessageRightViewHolder) holder).binding.textviewMessageTimeSender.setText(DateUtils.getFormattedTime(message.getDate()));

        } else if (holder instanceof MessageLeftViewHolder) {

final GroupChatEntity message = mMessagesList.get(position);
        ((MessageLeftViewHolder) holder).binding.textviewMessageReceiver.setText(message.getChatContent());
        //Adding senderName to the received message view
        ((MessageLeftViewHolder) holder).binding.senderName.setText(message.getSenderId());
        ((MessageLeftViewHolder) holder).binding.textviewMessageReceiverTime.setText(DateUtils.getFormattedTime(message.getDate()));

        }
        }

@Override
public int getItemViewType(int position) {
        GroupChatEntity itemMessage = mMessagesList.get(position);

        if (itemMessage.getChatType().equalsIgnoreCase(Constants.MESSAGE_SENDER))
        return VIEW_RECEIVER_MESSAGE;
        else
        return VIEW_SENDER_MESSAGE;

        }

@Override
public int getItemCount() {
        return mMessagesList.size();
        }

private class MessageLeftViewHolder extends RecyclerView.ViewHolder {
    MessageViewReceiverBinding binding;

    MessageLeftViewHolder(MessageViewReceiverBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

private class MessageRightViewHolder extends RecyclerView.ViewHolder {
    MessageViewSenderBinding binding;

    MessageRightViewHolder(MessageViewSenderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
}