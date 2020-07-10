package com.saraswati.jain.jainsaraswati.Models;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    @SerializedName("text")
    private String text;
    @SerializedName("category")
    private String category;


    public Suggestion(String text, String category) {
        this.text = text;
        this.category = category;
    }

}
