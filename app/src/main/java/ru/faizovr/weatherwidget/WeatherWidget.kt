package ru.faizovr.weatherwidget

import android.app.PendingIntent
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
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
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            Toast.makeText(context, "Widget has been updated! ", Toast.LENGTH_SHORT).show()
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
        val widgetText = context.getString(R.string.appwidget_text)
//        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        val refreshIntent = Intent(context, WeatherWidget::class.java)
        refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        refreshIntent.putExtra("appWidgetId", appWidgetId)
        val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)
        views.setOnClickPendingIntent(R.id.text_city, refreshPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        loadWeatherForecast(widgetText, context, views)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun loadWeatherForecast(
        city: String,
        context: Context,
        views: RemoteViews
    ) {
        val call = WeatherServiceBuilder.buildService().getCurrentWeatherData(city, API_KEY)
        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("CheckResult")
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
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
        //        private const val BASE_URL = "api.openweathermap.org/"
    }
}
