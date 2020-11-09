package ru.faizovr.weatherwidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
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

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val widgetText = context.getString(R.string.appwidget_text)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

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
        val body = call.execute().body()
        Log.d(this@WeatherWidget.toString(), "loadWeatherForecast: $body")
//        call.enqueue(object : Callback<WeatherResponse> {
//            @SuppressLint("CheckResult")
//            override fun onResponse(
//                call: Call<WeatherResponse>,
//                response: Response<WeatherResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val weatherResponse = response.body()
//                    if (weatherResponse != null) {
//                        Log.d(this@WeatherWidget.toString(), "onResponse: ${weatherResponse.main.temp} + ${weatherResponse.weather[0].description}")
//                        views.setTextViewText(
//                            R.id.text_temperature,
//                            weatherResponse.main.temp.toString()
//                        )
//                        views.setTextViewText(
//                            R.id.text_weather_description,
//                            weatherResponse.weather[0].description
//                        )
////                        val imageBitmap = Glide
////                            .with(context)
////                            .asBitmap()
////                            .load("$ICON_BASE_URL${weatherResponse.weather[0].icon}.png")
////                            .submit()
////                            .get()
////                        views.setImageViewBitmap(R.id.image_weather, imageBitmap)
//                        views.setTextViewText(R.id.tv_city, city) //
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
//                Log.e(this@WeatherWidget.toString(), "onFailure: api call failed")
//            }
//
//        })
    }

    companion object {
        private const val API_KEY = "eea8689af3e42649b7c92028787960b3"
        private const val ICON_BASE_URL = "http://openweathermap.org/img/w/"
//        private const val BASE_URL = "api.openweathermap.org/"
    }
}