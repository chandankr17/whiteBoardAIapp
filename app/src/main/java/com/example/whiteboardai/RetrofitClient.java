package com.example.whiteboardai

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

private const val BASE_URL = "http://172.19.15.142:5000/"

private val logging = HttpLoggingInterceptor().apply {
level = HttpLoggingInterceptor.Level.BODY
    }

private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

val api: ApiService by lazy {
    Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}
}