package ru.faizovr.weatherwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.request.target.AppWidgetTarget
import ru.faizovr.weatherwidget.data.model.WeatherModel
import ru.faizovr.weatherwidget.data.network.GlideApp
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.repository.Repository


class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        // There may be multiple widgets active, so update all of them
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, views, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, views: RemoteViews, appWidgetId: Int) {
        Repository().loadCurrentWeather(object : WeatherResponseCallback {
            @SuppressLint("CheckResult")
            override fun onSuccess(weatherModel: WeatherModel) {
                Log.d(TAG, "onSuccess: setDataToView")
                setDataToWidgetViews(context, views, appWidgetId, weatherModel)
            }

            override fun onLoading(isLoading: Boolean) {
                // show loading, retry call (how to retry call within the call ?)
                Log.d(TAG, "onLoading: setDataToView")
                setLoadingState(context, appWidgetId, views)
            }

            override fun onError(t: Throwable) {
                // show error, may be throw t
                Log.d(TAG, "onError: setDataToView")
                setErrorState(context, appWidgetId, views)
            }
        })
    }

    private fun setUpdateButton(context: Context, appWidgetId: Int, views: RemoteViews) {
        val refreshIntent = Intent(context, this::class.java)
        refreshIntent.action = "ru.faizovr.weatherwidget.REFRESH"
        refreshIntent.putExtra("appWidgetId", appWidgetId)
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.frame_weather, refreshPendingIntent)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: ")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: ")
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent.toString()} ${intent?.extras?.keySet()?.map {it.toString()}}")
        if (intent?.action == "ru.faizovr.weatherwidget.REFRESH") {
            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
            val appWidgetId = intent.extras?.getInt("appWidgetId")
            if (context != null && appWidgetId != null) {
                setLoadingState(context, appWidgetId, views)
            }
            if (context != null && appWidgetId != null) {
                updateWidget(context, views, appWidgetId)
            }
        }
        if (intent?.action == "android.appwidget.action.APPWIDGET_UPDATE") {
            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
            val appWidgetManager = AppWidgetManager.getInstance(context?.applicationContext)
            val appWidgetIds = intent.extras?.getIntArray("appWidgetIds")
            if (context != null && appWidgetIds != null) {
                for (appWidgetId in appWidgetIds) {
                    setUpdateButton(context, appWidgetId, views)
                    appWidgetManager.updateAppWidget(appWidgetIds, views)
                }
            }
        }
    }

    private fun setDataToWidgetViews(context: Context, views: RemoteViews, appWidgetId: Int, weatherModel: WeatherModel) {
        views.setTextViewText(
            R.id.text_weather_temperature,
            "${weatherModel.temp}° "
        )
        views.setTextViewText(
            R.id.text_weather_description,
            weatherModel.description
        )
        val awt: AppWidgetTarget = object : AppWidgetTarget(
            context.applicationContext,
            R.id.image_weather,
            views,
            appWidgetId
        ) {}
        GlideApp
            .with(context)
            .asBitmap()
            .load(weatherModel.iconUrl)
            .into(awt)
        setNormalState(context, appWidgetId, views)
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

    private fun setLoadingState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarVisible(views)
        setContentGone(views)
        setErrorMessageGone(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setNormalState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentVisible(views)
        setErrorMessageGone(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setErrorState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentGone(views)
        setErrorMessageVisible(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private const val  TAG = "WeatherWidget"
    }
}
