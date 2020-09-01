package com.example.thehustler.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ReviewModel {
    private String reviewerId;
    private String reviewText;

    @ServerTimestamp
    private Date reviewdate;

    public ReviewModel() {
    }

    public ReviewModel(String reviewerId, String reviewText) {
        this.reviewerId = reviewerId;
        this.reviewText = reviewText;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public Date getReviewdate() {
        return reviewdate;
    }



    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setReviewdate(Date reviewdate) {
        this.reviewdate = reviewdate;
    }

}
