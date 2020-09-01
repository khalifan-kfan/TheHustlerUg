package com.example.thehustler.classes;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;


public class postId {

    @Exclude
    public String postId;
    public <T extends postId> T withID(@NonNull final String id){
        this.postId = id;
        return(T) this;
    }

}
