package ru.faizovr.weatherwidget.data.network

import ru.faizovr.weatherwidget.domain.model.WeatherModel

interface WeatherResponseCallback {
    fun onSuccess(weatherModel: WeatherModel)
    fun onLoading(isLoading: Boolean)
    fun onError(t: Throwable)
}