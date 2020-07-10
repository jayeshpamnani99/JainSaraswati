package com.saraswati.jain.jainsaraswati.Apis;

import com.saraswati.jain.jainsaraswati.Models.Book;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BookApi {

    @GET("book/books")
    Call<List<Book>> getBooks();
}
