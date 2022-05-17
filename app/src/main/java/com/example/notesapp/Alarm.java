package com.example.notesapp;

public class Alarm {
    long time;
    String title;

    public Alarm(){

    }

    public Alarm(long time, String title) {
        this.time = time;
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
