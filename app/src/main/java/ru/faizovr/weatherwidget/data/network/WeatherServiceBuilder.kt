package ru.faizovr.weatherwidget.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherServiceBuilder {

    private const val BASE_URL = "https://api.openweathermap.org/"

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    private val unsafeClient: OkHttpClient = UnsafeOkHttpClient().getUnsafeOkHttpClient().apply {
        this.addInterceptor(interceptor)
    }.build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(unsafeClient)
        .build()

    fun buildService(): WeatherService =
        retrofit.create(WeatherService::class.java)

}