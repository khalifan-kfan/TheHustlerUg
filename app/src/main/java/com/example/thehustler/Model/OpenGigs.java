package com.example.thehustler.Model;


import com.example.thehustler.classes.UserId;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

public class OpenGigs extends UserId {

    private String gig_description;
    private String to_id;
    private String from_id;
    private String  status;
    private String  end_time;
    private  String start_time;
    private String  ref_id;


    @ServerTimestamp
    private  String  gig_date;

    private String gig_image;



    public OpenGigs(String gig_description, String to_id, String from_id, String status,
                    String end_time, String gig_image, String ref_id, String start_time) {
        this.gig_description = gig_description;
        this.to_id = to_id;
        this.from_id = from_id;
        this.status = status;
        this.end_time = end_time;
        this.gig_image = gig_image;
        this.ref_id = ref_id;
        this.start_time = start_time;
    }

    public OpenGigs() {
    }

    public String getRef_id() {
        return ref_id;
    }

    public void setRef_id(String ref_id) {
        this.ref_id = ref_id;
    }

    public String getTo_id() {
        return to_id;
    }

    public void setTo_id(String to_id) {
        this.to_id = to_id;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getGig_description() {
        return gig_description;
    }

    public void setGig_description(String gig_description) {
        this.gig_description = gig_description;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
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
