package ru.faizovr.weatherwidget.data.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.faizovr.weatherwidget.data.model.WeatherResponse
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.network.WeatherServiceBuilder
import kotlin.math.roundToInt

class Repository {

//    interface WeatherResponseCallback {
//        fun onSuccess(temp: Int, description: String, iconUrl: String)
//        fun onLoading(isLoading: Boolean)
//        fun onError(t: Throwable)
//    }

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
                        weatherResponseCallback.onSuccess(
                            weatherResponse.main.temp.roundToInt(),
                            weatherResponse.weather[0].description,
                            weatherResponse.weather[0].icon
                        )
                    }
//                    setNormalState(context, appWidgetId, views)
                } else {
//                    setErrorState(context, appWidgetId, views)
                    weatherResponseCallback.onLoading(true)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: api call failed")
//                setErrorState(context, appWidgetId, views)
                weatherResponseCallback.onError(t)
            }
        })
    }

    companion object {
        private const val STR_CITY = "Moscow"
        private const val STR_LOCALE = "ru"
        private const val STR_METRIC = "metric"
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
        private const val TAG = "Repository: "
    }
}