package com.example.thehustler.Model;

import com.example.thehustler.classes.UserId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comments extends UserId {
    private String answer, user_id;

    @ServerTimestamp
    private Date timestamp;

    public Comments(String answer, String user_id) {
        this.answer = answer;
        this.user_id = user_id;

    }

    public Comments() {
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUserID() {
        return user_id;
    }

    public void setUserID(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
