package com.example.thehustler.Model;


import com.example.thehustler.classes.UserId;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

public class OpenGigs extends UserId {

    private String gig_description;
    private String from_id;

    @ServerTimestamp
    private  String  gig_date;
    private String gig_image;


    public OpenGigs(String gig_description, String gig_image) {
        this.gig_description = gig_description;
        this.gig_image = gig_image;
    }

    public OpenGigs() {
    }

    public String getGig_description() {
        return gig_description;
    }

    public void setGig_description(String gig_description) {
        this.gig_description = gig_description;
    }

    public String getGig_date() {
        return gig_date;
    }

    public void setGig_date(String gig_date) {
        this.gig_date = gig_date;
    }

    public String getGig_image() {
        return gig_image;
    }

    public void setGig_image(String gig_image) {
        this.gig_image = gig_image;
    }
}
