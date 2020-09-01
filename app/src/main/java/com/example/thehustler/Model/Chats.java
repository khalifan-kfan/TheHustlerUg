package com.example.thehustler.Model;

import java.util.Date;

public class Chats {

    Date latest_update;
    String  ChatterId;

    public Chats() {
    }

    public Chats(Date latest_update, String chatterId) {
        this.latest_update = latest_update;
        ChatterId = chatterId;
    }

    public Date getLatest_update() {
        return latest_update;
    }

    public void setLatest_update(Date latest_update) {
        this.latest_update = latest_update;
    }

    public String getChatterId() {
        return ChatterId;
    }

    public void setChatterId(String ChatterId) {
        this.ChatterId = ChatterId;
    }
}
