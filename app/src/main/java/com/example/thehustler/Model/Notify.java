package com.example.thehustler.Model;

import java.util.Date;
import com.google.firebase.firestore.ServerTimestamp;

public class Notify  {
    private String status;
    private String notId;
    private String postId;


    @ServerTimestamp
    private Date timestamp;

    public Notify() {
    }

    public Notify(String status, String notId, String postId) {
        this.status = status;
        this.notId = notId;
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotId() {
        return notId;
    }

    public void setNotId(String notId) {
        this.notId = notId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



}
