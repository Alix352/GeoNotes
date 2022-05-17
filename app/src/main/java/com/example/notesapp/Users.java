package com.example.notesapp;

public class Users {

    private String fullName, email;
    private String latitude, longitude;

    public Users(){

    }

    public Users(String fullName){
        this.fullName = fullName;
    }

    public Users(String fullName, String email){
        this.fullName = fullName;
        this.email = email;
    }

    public Users(String fullName, String latitude, String longitude){
        this.fullName = fullName;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



}
