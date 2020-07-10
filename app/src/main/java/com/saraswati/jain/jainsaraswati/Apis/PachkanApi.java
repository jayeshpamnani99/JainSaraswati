package com.saraswati.jain.jainsaraswati.Apis;

import com.saraswati.jain.jainsaraswati.Models.Music;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PachkanApi {

    @GET("music/pachkans")
    Call<List<Music>> getPachkan();
}
