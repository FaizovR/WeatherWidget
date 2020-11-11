package ru.faizovr.weatherwidget.data.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.faizovr.weatherwidget.data.model.WeatherModel
import ru.faizovr.weatherwidget.data.model.WeatherResponse
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.network.WeatherServiceBuilder
import kotlin.math.roundToInt

class Repository {

    private val call: Call<WeatherResponse> = WeatherServiceBuilder.buildService().getCurrentWeatherData(
        STR_CITY,
        STR_LOCALE,
        STR_METRIC,
        API_KEY
    )

    fun loadCurrentWeather(weatherResponseCallback: WeatherResponseCallback) {
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        val weatherModel = setModelData(weatherResponse)
                        weatherResponseCallback.onSuccess(weatherModel)
                        Log.d(this@Repository.toString(), "onResponse: loadCurrentWeather insides")
                    }
                } else {
                    weatherResponseCallback.onLoading(true)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherResponseCallback.onError(t)
            }
        })
    }

    private fun setModelData(weatherResponse: WeatherResponse): WeatherModel =
        WeatherModel(
            weatherResponse.main.temp.roundToInt(),
            weatherResponse.weather[0].description,
            "$ICON_BASE_URL${weatherResponse.weather[0].icon}.png")

    companion object {
        private const val STR_CITY = "Moscow"
        private const val STR_LOCALE = "ru"
        private const val STR_METRIC = "metric"
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
    }
}