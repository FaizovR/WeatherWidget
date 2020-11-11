package ru.faizovr.weatherwidget.presentation

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews

interface WeatherWidgetContract {

    interface ViewInterface {
        fun setLoadingState(context: Context, appWidgetId: Int, views: RemoteViews)
        fun setNormalState(context: Context, appWidgetId: Int, views: RemoteViews)
        fun setErrorState(context: Context, appWidgetId: Int, views: RemoteViews)
        fun setUpdateButton(context: Context, appWidgetId: Int)
    }

    interface WeatherWidgetPresenterInterface {
        fun onWidgetClickedForUpdate(context: Context)
        fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray)
    }
}