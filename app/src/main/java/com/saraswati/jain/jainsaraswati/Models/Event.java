package com.saraswati.jain.jainsaraswati.Models;

import com.google.gson.annotations.SerializedName;

public class Event {
    @SerializedName("day")
    private int day;
    @SerializedName("month")
    private int month;
    @SerializedName("events")
    private String[] events;

    public Event(int day, int month, String[] events) {
        this.day = day;
        this.month = month;
        this.events = events;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String[] getEvents() {
        return events;
    }

    public void setEvents(String[] events) {
        this.events = events;
    }
}
