package ru.faizovr.weatherwidget.data.network

interface WeatherResponseCallback {
    fun onSuccess(temp: Int, description: String, iconUrl: String)
    fun onLoading(isLoading: Boolean)
    fun onError(t: Throwable)
}