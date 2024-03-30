package com.example.inguanalchat;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderName, message, date;

        public MessageViewHolder(View view) {
            super(view);
            senderName = view.findViewById(R.id.textViewSenderName);
            message = view.findViewById(R.id.textViewMessage);
            date = view.findViewById(R.id.textViewDate);
        }
    }

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_message_item, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.senderName.setText(message.getName());
        holder.message.setText(message.getMessage());
        holder.date.setText(message.getDate());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}