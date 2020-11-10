package ru.faizovr.weatherwidget.data.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.faizovr.weatherwidget.data.model.WeatherModel
import ru.faizovr.weatherwidget.data.model.WeatherResponse
import ru.faizovr.weatherwidget.data.network.Resource
import ru.faizovr.weatherwidget.data.network.WeatherServiceBuilder

class Repository(private val weatherModel: WeatherModel) {

    private val call: Call<Resource<WeatherResponse>> = WeatherServiceBuilder.buildService().getCurrentWeatherData(
        STR_CITY,
        STR_LOCALE,
        STR_METRIC,
        API_KEY
    )

    fun loadCurrentWeather(city: String) {
        call.enqueue(object : Callback<Resource<WeatherResponse>> {
            override fun onResponse(
                call: Call<Resource<WeatherResponse>>,
                response: Response<Resource<WeatherResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data
//                    setNormalState(context, appWidgetId, views)
                } else {
//                    setErrorState(context, appWidgetId, views)
                }
            }

            override fun onFailure(call: Call<Resource<WeatherResponse>>, t: Throwable) {
                Log.e(TAG, "onFailure: api call failed")
//                setErrorState(context, appWidgetId, views)
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