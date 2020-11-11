package ru.faizovr.weatherwidget

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
//        Загрузить данные с апи один раз
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
//      Обновление стейта всех виджетов на лоадинг
        for (appWidgetId in appWidgetIds) {
            setLoadingState(context, appWidgetId, views)
//            updateWidget(context, views, appWidgetId)
        }
//        Обновить все виджеты
//        When Api Data is ready, update data in views, and set normal state or Error state
        for (appWidgetId in appWidgetIds) {
            setUpdateButton(context, appWidgetId)
            //            When Api Data is ready, update data in views, and set normal state or Error state
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



    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: ")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: ")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(
            TAG,
            "onReceive: ${intent.toString()} ${intent?.extras?.keySet()?.map { it.toString() }}"
        )

        if (intent?.action == ACTION_REFRESH) {
            // Вынести в отдельный метод
            val extras = intent.extras
            if (extras != null && context != null) {
                val appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val views = RemoteViews(context?.packageName, R.layout.weather_widget)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
//                  Начать загрузку данных
                    for (appWidgetId in appWidgetIds) {
                        setLoadingState(context, appWidgetId,views)
//                        updateWidget(context, views, appWidgetId)
                    }
//                  Когда данные вернулить обновить вью через стейт
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

    fun setLoadingState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarVisible(views)
        setContentGone(views)
        setErrorMessageGone(views)
        updateAppWidget(context, appWidgetId, views)
    }

    fun setNormalState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentVisible(views)
        setErrorMessageGone(views)
        updateAppWidget(context, appWidgetId, views)
    }

    fun setErrorState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentGone(views)
        setErrorMessageVisible(views)
        updateAppWidget(context, appWidgetId, views)
    }

    private fun updateAppWidget(context: Context, appWidgetId: Int, views: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
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
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
        private const val ACTION_REFRESH = "ru.faizovr.weatherwidget.REFRESH"
        private const val APP_WIDGET_ID = "appWidgetId"
    }
}
