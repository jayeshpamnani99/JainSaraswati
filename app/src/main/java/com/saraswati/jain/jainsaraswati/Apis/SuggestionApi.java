package com.saraswati.jain.jainsaraswati.Apis;

import com.saraswati.jain.jainsaraswati.Models.ResponseBody;
import com.saraswati.jain.jainsaraswati.Models.Suggestion;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SuggestionApi {

    @POST("suggestion/")
    @FormUrlEncoded
    Call<ResponseBody> sendSuggestion(@Field("text") String text , @Field("type") String type);

}
