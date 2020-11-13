package ru.faizovr.weatherwidget.data.model

import com.google.gson.annotations.SerializedName


class Main(
    @SerializedName("temp")
    val temperature: Double
)