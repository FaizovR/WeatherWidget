package ru.faizovr.weatherwidget.presentation.presenter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import ru.faizovr.weatherwidget.presentation.WeatherWidgetContract

class WeatherWidgetPresenter(private val viewInterface: WeatherWidgetContract.ViewInterface, private val views: RemoteViews): WeatherWidgetContract.WeatherWidgetPresenterInterface {

    private var localAppWidgetIds: IntArray? = null

    override fun onWidgetClickedForUpdate(context: Context) {
        if (localAppWidgetIds != null) {
            val lAppWidgetIds: IntArray = localAppWidgetIds as IntArray
//           getDataFromApi
            for (appWidgetId in lAppWidgetIds) {
                viewInterface.setLoadingState(context, appWidgetId, views)
            }
//            When Api Data is ready, update data in views, and set normal state or Error state
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (!isArraysIsSame(appWidgetIds)) {
            this.localAppWidgetIds = appWidgetIds
            for (appWidgetId in appWidgetIds) {
                viewInterface.setUpdateButton(context, appWidgetId)
            }
        }
//          loadWeatherForecast("Moscow", context, views, appWidgetId)

        for (appWidgetId in appWidgetIds) {
//            When Api Data is ready, update data in views, and set normal state or Error state
        }
    }

    private fun isArraysIsSame(newAppWidgetIds: IntArray): Boolean {
        if (localAppWidgetIds != null) {
            val lAppWidgetIds: IntArray = localAppWidgetIds as IntArray
            if (lAppWidgetIds.size != newAppWidgetIds.size) {
                return false
            }
            for (iterator in lAppWidgetIds) {
                if (lAppWidgetIds[iterator] != newAppWidgetIds[iterator])
                    return false
            }
            return true
        }
        return false
    }
}