package com.example.myapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.databinding.MessageViewReceiverBinding;
import com.example.myapp.databinding.MessageViewSenderBinding;
import com.example.myapp.db.entity.GroupChatEntity;
import com.example.myapp.db.entity.LedgerEntity;

import java.util.List;

public class LedgerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<LedgerEntity> ledgerList;
    Context activity;
    public final int VIEW_RECEIVER_MESSAGE = 1, VIEW_SENDER_MESSAGE = 2;
    int lastPosition = 0;


    public LedgerAdapter(Context context, List<LedgerEntity> objects) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ledgerList = objects;
        activity = context;
    }

    public void refresh(List<LedgerEntity> mMessagesList) {
        this.ledgerList = mMessagesList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return ledgerList.size();
    }
}
