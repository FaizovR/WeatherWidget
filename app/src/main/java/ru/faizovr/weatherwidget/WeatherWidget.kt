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
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.faizovr.weatherwidget.network.WeatherResponse
import ru.faizovr.weatherwidget.network.WeatherServiceBuilder

/**
 * Implementation of App Widget functionality.
 */
class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        // There may be multiple widgets active, so update all of them
        val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)
        for (appWidgetId in appWidgetIds) {
            setUpdateButton(context, appWidgetId, views)
            setStopUpdate(context, appWidgetId, views)
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    private fun setStopUpdate(context: Context, appWidgetId: Int, views: RemoteViews) {
        val refreshIntent = Intent(context, this::class.java)
        refreshIntent.action = "ru.faizovr.weatherwidget.STOPREFRESH"
        refreshIntent.putExtra("appWidgetId", appWidgetId)
        val refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.pb_weather_loading, refreshPendingIntent)
    }

    private fun setUpdateButton(context: Context, appWidgetId: Int, views: RemoteViews) {
        val refreshIntent = Intent(context, this::class.java)
        refreshIntent.action = "ru.faizovr.weatherwidget.REFRESH"
        refreshIntent.putExtra("appWidgetId", appWidgetId)
        val refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.frame_weather, refreshPendingIntent)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: ")
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: ")
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent.toString()}")
        if (intent?.action == "ru.faizovr.weatherwidget.REFRESH") {
            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
            setLoadingState(views)
            val appWidgetManager = AppWidgetManager.getInstance(context?.applicationContext)
            // get appWidgetId
            val appWidgetId = intent.extras?.getInt("appWidgetId")
            if (context != null && appWidgetId != null) {
                Log.d(TAG, "loadData: ")
                loadWeatherForecast("Moscow", context, views, appWidgetId)
                Log.d(TAG, "onReceive: ")
            }
            // load data again
            if (appWidgetId != null) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
        if (intent?.action == "ru.faizovr.weatherwidget.STOPREFRESH") {

            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
            setNormalState(views)
            val appWidgetManager = AppWidgetManager.getInstance(context?.applicationContext)
            // get appWidgetId
            val appWidgetId = intent.extras?.getInt("appWidgetId")
            // load data again
            if (appWidgetId != null) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
        if (intent?.action == "ru.faizovr.weatherwidget.REMOTEUPDATE") {

        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)

//        loadWeatherForecast("Moscow", context, views)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun loadWeatherForecast(
        city: String,
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        val call = WeatherServiceBuilder.buildService().getCurrentWeatherData()//city, API_KEY)
        call.enqueue(object : Callback<WeatherResponse> {
//            @SuppressLint("CheckResult")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        Log.d(this@WeatherWidget.toString(), "onResponse: ${weatherResponse.main.temp} + ${weatherResponse.weather[0].description}")
                        views.setTextViewText(
                            R.id.text_weather_temperature,
                            weatherResponse.main.temp.toString()
                        )
                        views.setTextViewText(
                            R.id.text_weather_description,
                            weatherResponse.weather[0].description
                        )
//                        val imageBitmap = Glide
//                            .with(context)
//                            .asBitmap()
//                            .load("$ICON_BASE_URL${weatherResponse.weather[0].icon}.png")
//                            .submit()
//                            .get()
//                        views.setImageViewBitmap(R.id.image_weather, imageBitmap)
                        views.setTextViewText(R.id.text_city, city) //
                    }
                    setNormalState(views)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(this@WeatherWidget.toString(), "onFailure: api call failed")
                setErrorState(views)
            }

        })
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

    companion object {
        private const val  TAG = "WeatherWidget"
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
        //        private const val BASE_URL = "api.openweathermap.org/"
    }
}
