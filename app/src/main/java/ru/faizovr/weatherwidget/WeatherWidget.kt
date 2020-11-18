package ru.faizovr.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.request.target.AppWidgetTarget
import ru.faizovr.weatherwidget.data.model.WeatherModel
import ru.faizovr.weatherwidget.data.network.GlideApp
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.repository.Repository
import ru.faizovr.weatherwidget.presentation.view.SettingsActivity

class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val thisWidget = ComponentName(context, this.javaClass)
        val allWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(thisWidget)
        setSettingsButton(context, allWidgetIds)
        setUpdateButton(context, allWidgetIds)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        setLoadingState(context, allWidgetIds, views)
        updateWidgetViews(context, views, allWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_UPDATE_WEATHER) {
            val extras: Bundle? = intent.extras
            if (extras != null && context != null) {
                onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf())
            }
        }
    }

    private fun updateWidgetViews(context: Context, views: RemoteViews, appWidgetIds: IntArray) {
        Repository().loadCurrentWeather(object : WeatherResponseCallback {
            override fun onSuccess(weatherModel: WeatherModel) {
                setDataToWidgetViews(context, views, appWidgetIds, weatherModel)
                setNormalState(context, appWidgetIds, views)
            }

            override fun onLoading(isLoading: Boolean) {
                setLoadingState(context, appWidgetIds, views)
            }

            override fun onError(t: Throwable) {
                setErrorState(context, appWidgetIds, views)
            }
        })
    }

    private fun setDataToWidgetViews(
        context: Context,
        views: RemoteViews,
        appWidgetIds: IntArray,
        weatherModel: WeatherModel
    ) {
        views.setTextViewText(
            R.id.text_weather_temperature,
            "${weatherModel.temp}° "
        )
        views.setTextViewText(
            R.id.text_weather_description,
            weatherModel.description
        )
        for (id in appWidgetIds) {
            GlideApp
                .with(context)
                .asBitmap()
                .load(weatherModel.iconUrl)
                .into(
                    AppWidgetTarget(
                        context.applicationContext,
                        R.id.image_weather,
                        views,
                        id
                    )
                )
        }
    }

    private fun setProgressBarVisibility(views: RemoteViews, visibility: Int) {
        views.setViewVisibility(R.id.pb_weather_loading, visibility)
    }

    private fun setErrorMessageVisibility(views: RemoteViews, visibility: Int) {
        views.setViewVisibility(R.id.text_error_message, visibility)
    }

    private fun setContentVisibility(views: RemoteViews, visibility: Int) {
        views.setViewVisibility(R.id.linear_layout, visibility)
        views.setViewVisibility(R.id.text_city, visibility)
    }

    private fun setLoadingState(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        setProgressBarVisibility(views, View.VISIBLE)
        setContentVisibility(views, View.GONE)
        setErrorMessageVisibility(views, View.GONE)
        updateAppWidgets(context, appWidgetIds, views)
    }

    private fun setNormalState(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        setProgressBarVisibility(views, View.GONE)
        setContentVisibility(views, View.VISIBLE)
        setErrorMessageVisibility(views, View.GONE)
        updateAppWidgets(context, appWidgetIds, views)
    }

    private fun setErrorState(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        setProgressBarVisibility(views, View.GONE)
        setContentVisibility(views, View.GONE)
        setErrorMessageVisibility(views, View.VISIBLE)
        updateAppWidgets(context, appWidgetIds, views)
    }

    private fun updateAppWidgets(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    private fun setUpdateButton(context: Context, appWidgetIds: IntArray) {
        val refreshIntent = Intent(context, this::class.java)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        refreshIntent.action = ACTION_UPDATE_WEATHER
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        val refreshPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.frame_weather, refreshPendingIntent)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    private fun setSettingsButton(context: Context, appWidgetIds: IntArray) {
        val intent = Intent(context, SettingsActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        views.setOnClickPendingIntent(R.id.btn_settings, pendingIntent)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    companion object {
        private const val ACTION_UPDATE_WEATHER = "ru.faizovr.weatherwidget.UPDATE_WEATHER"
    }
}
