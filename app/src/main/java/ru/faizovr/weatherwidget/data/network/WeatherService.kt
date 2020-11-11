package ru.faizovr.weatherwidget.data.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.faizovr.weatherwidget.data.model.WeatherResponse

interface WeatherService {
    @GET("data/2.5/weather")
    fun getCurrentWeatherData(
        @Query("q") city: String,
        @Query("lang") lang: String,
        @Query("units") metric: String,
        @Query("APPID") appId: String
    ): Call<WeatherResponse>
}