package ru.faizovr.weatherwidget.presentation.presenter

import android.appwidget.AppWidgetManager
import android.content.Context
import ru.faizovr.weatherwidget.presentation.WeatherWidgetContract

class WeatherWidgetPresenter(private val viewInterface: WeatherWidgetContract.ViewInterface): WeatherWidgetContract.WeatherWidgetPresenterInterface {

    private var appWidgetIds: IntArray? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
//            loadWeatherForecast("Moscow", context, views, appWidgetId)
        }
    }

}