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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.faizovr.weatherwidget.network.GlideApp
import ru.faizovr.weatherwidget.network.WeatherResponse
import ru.faizovr.weatherwidget.network.WeatherServiceBuilder
import ru.faizovr.weatherwidget.presentation.WeatherWidgetContract
import ru.faizovr.weatherwidget.presentation.presenter.WeatherWidgetPresenter
import kotlin.math.roundToInt

class WeatherWidget : AppWidgetProvider(), WeatherWidgetContract.ViewInterface {

    private var weatherWidgetPresenter: WeatherWidgetContract.WeatherWidgetPresenterInterface? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        val views: RemoteViews = RemoteViews(context.packageName, R.layout.weather_widget)
        weatherWidgetPresenter?.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        val views: RemoteViews = RemoteViews(context?.packageName, R.layout.weather_widget)
        weatherWidgetPresenter = WeatherWidgetPresenter(this, views)
    }

    override fun onDisabled(context: Context) {
        weatherWidgetPresenter = null
        Log.d(TAG, "onDisabled: ")
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent.toString()} ${intent?.extras?.keySet()?.map {it.toString()}}")
        if (intent?.action == "ru.faizovr.weatherwidget.REFRESH") {
            if (context != null) {
                weatherWidgetPresenter?.onWidgetClickedForUpdate(context)
            }
        }
    }

    private fun loadWeatherForecast(
        city: String,
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        val call = WeatherServiceBuilder.buildService().getCurrentWeatherData(
            city,
            "ru",
            "metric",
            API_KEY
        )
        call.enqueue(object : Callback<WeatherResponse> {
            //            @SuppressLint("CheckResult")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        Log.d(
                            TAG,
                            "onResponse: ${weatherResponse.main.temp} + ${weatherResponse.weather[0].description}"
                        )
                        views.setTextViewText(
                            R.id.text_weather_temperature,
                            "${weatherResponse.main.temp.roundToInt()}° "
                        )
                        views.setTextViewText(
                            R.id.text_weather_description,
                            weatherResponse.weather[0].description.capitalize()
                        )
                        val awt: AppWidgetTarget = object : AppWidgetTarget(context.applicationContext, R.id.image_weather, views, appWidgetId) {}
                        GlideApp.with(context.applicationContext).asBitmap()
                                .load("$ICON_BASE_URL${weatherResponse.weather[0].icon}.png").into(awt)
                    }
                    setNormalState(context, appWidgetId, views)
                } else {
                    setErrorState(context, appWidgetId, views)

                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: api call failed")
                setErrorState(context, appWidgetId, views)
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

    override fun setLoadingState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarVisible(views)
        setContentGone(views)
        setErrorMessageGone(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun setNormalState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentVisible(views)
        setErrorMessageGone(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun setErrorState(context: Context, appWidgetId: Int, views: RemoteViews) {
        setProgressBarGone(views)
        setContentGone(views)
        setErrorMessageVisible(views)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun setUpdateButton(context: Context, appWidgetId: Int) {
        val refreshIntent = Intent(context, this::class.java)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
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

    companion object {
        private const val  TAG = "WeatherWidget"
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
    }
}
