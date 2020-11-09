package ru.faizovr.weatherwidget.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather?q=Moscow&APPID=eea8689af3e42649b7c92028787960b3")
    fun getCurrentWeatherData(
//        @Query("city") city: String,
//        @Query("APPID") appId: String
    ): Call<WeatherResponse>
}