package com.example.thehustler.Model;



import com.example.thehustler.classes.postId;

import java.util.Date;
import java.util.List;

public class Blogpost extends postId {
    // for
    public  String re_post_desc;
    List<String>re_image_url,re_post_image_thumb;
    public Date re_timeStamp;

    public String user_id,description,re_postId;
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

    public Blogpost(String re_post_desc, List<String> re_image_url, List<String> re_post_image_thumb, Date re_timeStamp,String re_postId) {
        this.re_post_desc = re_post_desc;
        this.re_postId = re_postId;
        this.re_image_url = re_image_url;
        this.re_post_image_thumb = re_post_image_thumb;
        this.re_timeStamp = re_timeStamp;
    }

    public String getRe_post_desc() {
        return re_post_desc;
    }

    public void setRe_post_desc(String re_post_desc) {
        this.re_post_desc = re_post_desc;
    }

    public List<String> getRe_image_url() {
        return re_image_url;
    }

    public void setRe_image_url(List<String> re_image_url) {
        this.re_image_url = re_image_url;
    }

    public List<String> getRe_post_image_thumb() {
        return re_post_image_thumb;
    }

    public void setRe_post_image_thumb(List<String> re_post_image_thumb) {
        this.re_post_image_thumb = re_post_image_thumb;
    }

    public Date getRe_timeStamp() {
        return re_timeStamp;
    }

    public void setRe_timeStamp(Date re_timeStamp) {
        this.re_timeStamp = re_timeStamp;
    }

    public String getRe_postId() {
        return re_postId;
    }

    public void setRe_postId(String re_postId) {
        this.re_postId = re_postId;
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
