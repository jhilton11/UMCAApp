package com.appify.umcaapp.model;

public class Program {
    private String id;
    private String title;
    private String time;
    private String day;

    public Program(String id, String title, String time, String day) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.day = day;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
