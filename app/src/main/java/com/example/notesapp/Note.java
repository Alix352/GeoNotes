package com.example.notesapp;


public class Note {

    private String ID;
    private String title;
    private String content;
    private String date;
    private String time;
    private String noteType;
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;

    Note() {}

    Note(String title, String content, String date, String time){
        this.title = title;
        this.content = content;
        this.date = date;
        this.time = time;
    }

    Note(String title, String content, String date, String time, String noteType, int day, int month, int year, int hour, int minute){
        this.title = title;
        this.content = content;
        this.date = date;
        this.time = time;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.noteType = noteType;
    }
    Note(String title, String content, String date, String time, int day, int month, int year, int hour, int minute){
        this.title = title;
        this.content = content;
        this.date = date;
        this.time = time;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
    }
//
//    Note(String id, String title, String content, String date, String time, int day, int month, int year, int hour, int minute){
//        this.ID = id;
//        this.title = title;
//        this.content = content;
//        this.date = date;
//        this.time = time;
//        this.day = day;
//        this.month = month;
//        this.year = year;
//        this.hour = hour;
//        this.minute = minute;
//    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public int getDay() {
        return day;
    }
    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
