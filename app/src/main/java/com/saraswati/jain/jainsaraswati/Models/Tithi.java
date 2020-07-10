package com.saraswati.jain.jainsaraswati.Models;

import com.google.gson.annotations.SerializedName;

public class Tithi {
    @SerializedName("sud")
    private Boolean sud;
    @SerializedName("tithi")
    private int tithi;
    @SerializedName("month")
    private int month;

    public Tithi(Boolean sud, int tithi, int month) {
        this.sud = sud;
        this.tithi = tithi;
        this.month = month;
    }

    public Boolean getSud() {
        return sud;
    }

    public void setSud(Boolean sud) {
        this.sud = sud;
    }

    public int getTithi() {
        return tithi;
    }

    public void setTithi(int tithi) {
        this.tithi = tithi;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}
