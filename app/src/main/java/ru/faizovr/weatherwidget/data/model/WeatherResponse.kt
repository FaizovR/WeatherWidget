package ru.faizovr.weatherwidget.data.model


class WeatherResponse(
    val main: Main,
    val name: String,
    val weather: List<Weather>
)