package com.example.thehustler.classes;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class UserId {

    @Exclude
    public String Userid;
    public <T extends UserId> T withID(@NonNull final String id){
        this.Userid = id;
        return(T) this;
    }

}
