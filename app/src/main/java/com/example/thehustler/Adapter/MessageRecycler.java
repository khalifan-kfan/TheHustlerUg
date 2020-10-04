package com.example.thehustler.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thehustler.Model.Messages;
import com.example.thehustler.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageRecycler extends RecyclerView.Adapter<MessageRecycler.MessagesHolder> {

    static int MY_MESSAGE = 1;
    static int RECEIVED_MESSAGES = 2;

    String currentUserId;

    List<Messages> messages;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public MessageRecycler(String currentUserId){
        this.currentUserId = currentUserId;
        messages = new ArrayList<>();
    }

    @NonNull
    @Override
    public MessagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatCards;

        if(viewType == MY_MESSAGE){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_chat_card, parent, false);
        }else{
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receiver_chat_card, parent, false);
        }
        return new MessagesHolder(chatCards);
    }
    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderId().equals(currentUserId)){
            return MY_MESSAGE;
        }else{
            return RECEIVED_MESSAGES;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesHolder holder, int position) {
        holder.bindView(messages.get(position));

    }


    @Override
    public int getItemCount() {
        return  messages.size();
    }

    public void setData(List<Messages> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public class MessagesHolder extends RecyclerView.ViewHolder {

        TextView MessageTextView;
        TextView DateSentTextView;
        TextView SendernameTextView;
        private  View v;

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);
            v= itemView;
            MessageTextView = v.findViewById(R.id.message_tv);
            DateSentTextView = v.findViewById(R.id.dateSent_tv);
            SendernameTextView = v.findViewById(R.id.sender_name_tv);
        }
        public void bindView(Messages message) {
            SendernameTextView.setText(message.getSenderName());
            MessageTextView.setText(message.getMessageText());
            if(message.getDateSent() != null) {
                DateSentTextView.setText(format.format(message.getDateSent()));
            }else{
                DateSentTextView.setText(format.format(new Date()));
            }
        }

    }
}

