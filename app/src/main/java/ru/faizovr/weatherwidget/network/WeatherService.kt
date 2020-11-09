package ru.faizovr.weatherwidget.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    fun getCurrentWeatherData(
        @Query("q") city: String,
        @Query("lang") lang: String,
        @Query("units") metric: String,
        @Query("APPID") appId: String
    ): Call<WeatherResponse>
}