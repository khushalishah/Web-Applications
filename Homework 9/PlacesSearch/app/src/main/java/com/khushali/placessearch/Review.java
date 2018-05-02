package com.khushali.placessearch;

public class Review {

    String date;
    String name;
    float rating;
    String review;
    String picURL;
    String authURL;

    public Review() {
    }

    public Review(String date, String name, float rating, String review, String picURL, String authURL) {
        this.date = date;
        this.name = name;
        this.rating = rating;
        this.review = review;
        this.picURL = picURL;
        this.authURL = authURL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public void setAuthURL(String authURL) {
        this.authURL = authURL;
    }
}
