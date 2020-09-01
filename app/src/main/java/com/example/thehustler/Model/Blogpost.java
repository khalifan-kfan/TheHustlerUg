package com.example.thehustler.Model;



import com.example.thehustler.classes.postId;

import java.util.Date;
import java.util.List;

public class Blogpost extends postId {
    public String user_id,description;
    public Date timeStamp;
    List<String>image_url,post_image_thumb;

    public List<String> getImage_url() {
        return image_url;
    }

    public void setImage_url(List<String> image_url) {
        this.image_url = image_url;
    }

    public List<String> getPost_image_thumb() {
        return post_image_thumb;
    }

    public void setPost_image_thumb(List<String> post_image_thumb) {
        this.post_image_thumb = post_image_thumb;
    }

    public Blogpost(){

    }

    public Blogpost(String user_id, String description, Date timeStamp,List<String> image_url, List<String> post_image_thumb) {
        this.user_id = user_id;

        this.description = description;
        this.image_url = image_url;
        this.post_image_thumb = post_image_thumb;
        this.timeStamp = timeStamp;
    }





    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
