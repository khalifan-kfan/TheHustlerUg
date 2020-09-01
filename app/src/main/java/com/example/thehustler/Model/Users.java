package com.example.thehustler.Model;

import com.example.thehustler.classes.UserId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Users extends UserId {
    public String image;
    public List<String> name;
    public String thumb;


    @ServerTimestamp
    public Date created;

    public  String dob;
    public String sex;
    public String tele;
    public String city;
    public String work;
    public String country;

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }



    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public Users() {
    }

    public Users(String image, List<String> name, String thumb, String sex, String tele, String city, String country,String dob,String work) {
        this.image = image;
        this.name = name;
        this.thumb = thumb;

        this.sex = sex;
        this.tele = tele;
        this.city = city;
        this.country = country;
        this.dob = dob;
        this.work = work;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }



    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTele() {
        return tele;
    }

    public void setTele(String tele) {
        this.tele = tele;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }
}