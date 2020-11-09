package ru.faizovr.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId)

            val refreshIntent = Intent(context, WeatherWidget::class.java)
            refreshIntent.action = "ru.faizovr.weatherwidget.REFRESH"
            refreshIntent.putExtra("appWidgetId", appWidgetId)
            val refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)
            views.setOnClickPendingIntent(R.id.text_city, refreshPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == "ru.faizovr.weatherwidget.REFRESH") {
//            updateWeather

            Log.d("TAG", "onReceive: ")
            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
            setLoadingState(views)

            val appWidgetManager = AppWidgetManager.getInstance(context?.applicationContext)
            // get appWidgetId
            val appWidgetId = intent.extras?.getInt("appWidgetId")
            // load data again
            if (appWidgetId != null) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setProgressBarVisible(views: RemoteViews) {
        views.setViewVisibility(R.id.pb_weather_loading, View.VISIBLE)
    }

    private fun setProgressBarGone(views: RemoteViews) {
        views.setViewVisibility(R.id.pb_weather_loading, View.GONE)
    }

    private fun setErrorMessageVisible(views: RemoteViews) {
        views.setViewVisibility(R.id.text_error_message, View.VISIBLE)
    }

    private fun setErrorMessageGone(views: RemoteViews) {
        views.setViewVisibility(R.id.text_error_message, View.GONE)
    }

    private fun setContentVisible(views: RemoteViews) {
        views.setViewVisibility(R.id.linear_layout, View.VISIBLE)
        views.setViewVisibility(R.id.text_city, View.VISIBLE)
    }

    private fun setContentGone(views: RemoteViews) {
        views.setViewVisibility(R.id.linear_layout, View.GONE)
        views.setViewVisibility(R.id.text_city, View.GONE)
    }

    private fun setLoadingState(views: RemoteViews) {
        setProgressBarVisible(views)
        setContentGone(views)
        setErrorMessageGone(views)
    }

    private fun setNormalState(views: RemoteViews) {
        setProgressBarGone(views)
        setContentVisible(views)
        setErrorMessageGone(views)
    }

    private fun setErrorState(views: RemoteViews) {
        setProgressBarGone(views)
        setContentGone(views)
        setErrorMessageVisible(views)
    }
}
