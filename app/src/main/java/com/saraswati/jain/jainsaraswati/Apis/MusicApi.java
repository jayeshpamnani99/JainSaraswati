package com.saraswati.jain.jainsaraswati.Apis;

import com.saraswati.jain.jainsaraswati.Models.Music;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MusicApi {

    @GET("music/stavans")
    Call<List<Music>> getMusic();

}
