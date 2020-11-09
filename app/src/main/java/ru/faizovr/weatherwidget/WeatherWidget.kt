package ru.faizovr.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
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
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    val imageResWeather = R.color.teal_200
    val widgetTextTemperature = "8°"
    val widgetWeatherDescription = "Облачно, без осадков"
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.weather_widget)
    views.setTextViewText(R.id.text, widgetText)
    views.setImageViewResource(R.id.image_weather, imageResWeather)
    views.setTextViewText(R.id.text_temperature, widgetTextTemperature)
    views.setTextViewText(R.id.text_weather_description, widgetWeatherDescription)
//    views.setImageViewResource(R.id.view_background, R.color.light_blue_200)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}