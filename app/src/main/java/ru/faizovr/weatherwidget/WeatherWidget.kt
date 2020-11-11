package ru.faizovr.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import ru.faizovr.weatherwidget.data.model.WeatherModel
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.repository.Repository

class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: updateteteteetteteteetet")
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        setLoadingState(context, appWidgetIds, views)
        updateWidgetTextViews(views)
        updateAppWidget(context, appWidgetIds, views)
        for (appWidgetId in appWidgetIds) {
            setUpdateButton(context, appWidgetId)
        }
        setNormalState(context, appWidgetIds, views)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: ")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: ")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent.toString()} ${intent?.extras?.keySet()?.map { it.toString() }}")

        if (intent?.action == ACTION_REFRESH) {
            val extras = intent.extras
            if (extras != null && context != null) {
                val appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                val views = RemoteViews(context.packageName, R.layout.weather_widget)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    setLoadingState(context, appWidgetIds,views)
                    updateWidgetTextViews(views)
                    updateAppWidget(context, appWidgetIds, views)
                    setNormalState(context, appWidgetIds, views)
                }
            }
        }
    }

    private fun updateWidgetTextViews(views: RemoteViews) {
        Log.d(TAG, "updateWidgetTextViews: update view")
        Repository().loadCurrentWeather(object : WeatherResponseCallback {
            override fun onSuccess(weatherModel: WeatherModel) {
                setTextDataToWidgetViews(views, weatherModel)
                Log.d(TAG, "onSuccess: setDataToView")
            }

            override fun onLoading(isLoading: Boolean) {
                // show loading, retry call (how to retry call within the call ?)
                Log.d(TAG, "onLoading: setDataToView")
//                setLoadingState(context, appWidgetId, views)
            }

            override fun onError(t: Throwable) {
                // show error, may be throw t
                Log.d(TAG, "onError: setDataToView")
//                setErrorState(context, appWidgetId, views)
            }
        })
    }

    private fun setTextDataToWidgetViews(views: RemoteViews, weatherModel: WeatherModel) {
        views.setTextViewText(
            R.id.text_weather_temperature,
            "${weatherModel.temp}° "
        )
        views.setTextViewText(
            R.id.text_weather_description,
            weatherModel.description
        )
//        val awt: AppWidgetTarget = object : AppWidgetTarget(
//            context.applicationContext,
//            R.id.image_weather,
//            views,
//            appWidgetId
//        ) {}
//        GlideApp
//            .with(context)
//            .asBitmap()
//            .load((weatherModel.iconUrl))
//            .into(awt)
//        setNormalState(context, appWidgetId, views)
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
        updateAppWidget(context, appWidgetIds, views)
        Log.d(TAG, "setLoadingState: loading")
    }

    private fun setNormalState(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        setProgressBarVisibility(views, View.GONE)
        setContentVisibility(views, View.VISIBLE)
        setErrorMessageVisibility(views, View.GONE)
        updateAppWidget(context, appWidgetIds, views)
        Log.d(TAG, "setNormalState: normal")
    }

    private fun setErrorState(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        setProgressBarVisibility(views, View.GONE)
        setContentVisibility(views, View.GONE)
        setErrorMessageVisibility(views, View.VISIBLE)
        updateAppWidget(context, appWidgetIds, views)
        Log.d(TAG, "setErrorState: error")
    }

    private fun updateAppWidget(context: Context, appWidgetIds: IntArray, views: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetIds, views)
        Log.d(TAG, "updateAppWidget: update app widget")
    }

    private fun setUpdateButton(context: Context, appWidgetId: Int) {
        val refreshIntent = Intent(context, this::class.java)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        refreshIntent.action = ACTION_REFRESH
        refreshIntent.putExtra(APP_WIDGET_ID, appWidgetId)
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.frame_weather, refreshPendingIntent)
    }

    companion object {
        private const val TAG = "WeatherWidget"
        private const val ACTION_REFRESH = "ru.faizovr.weatherwidget.REFRESH"
        private const val APP_WIDGET_ID = "appWidgetId"
    }
}
