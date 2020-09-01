package com.example.thehustler.Model;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Messages {
    private String senderId;
    private  String senderName;
    private String messageText;

    @ServerTimestamp
    private Date dateSent;


    public Messages(){}


    public Messages(String senderId, String senderName, String messageText) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;

    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
}
