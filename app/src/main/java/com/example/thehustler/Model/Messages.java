package com.example.thehustler.Model;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Messages {
    private String senderId;
    private  String senderName;
    private String messageText;
    //image url variable
    // type text or image
    private List<String> images;
    private String type;

    @ServerTimestamp
    private Date dateSent;


    public Messages(){}


    public Messages(String senderId, String senderName, String messageText, List<String> images,String type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.images = images;
        this.type = type;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
