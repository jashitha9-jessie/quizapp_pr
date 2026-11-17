package com.example.quizapp.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {

    // https://opentdb.com/api.php?amount=10&category={id}&difficulty={level}&type=multiple
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 10,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String = "multiple"
    ): QuestionResponse

    // https://opentdb.com/api_category.php
    @GET("api_category.php")
    suspend fun getCategories(): CategoryResponse
}

object RetrofitClient {
    val api: TriviaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApi::class.java)
    }
}
