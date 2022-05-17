package com.example.notesapp;

public class PendingUsers {
    private String uid, name;

    public PendingUsers(){

    }

    public PendingUsers(String uid){
        this.uid = uid;
    }

    public PendingUsers(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }



    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
