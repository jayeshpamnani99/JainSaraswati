package com.saraswati.jain.jainsaraswati.Models;

import com.google.gson.annotations.SerializedName;

public class Music {
    @SerializedName("id")
    private Long id;
    @SerializedName("title")
    private String title;
    @SerializedName("url")
    private String url;
    @SerializedName("category")
    private String category;

    public Music(Long id, String title, String url, String category) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.category = category;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
