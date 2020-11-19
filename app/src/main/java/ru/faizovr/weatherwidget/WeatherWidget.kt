package ru.faizovr.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.AppWidgetTarget
import ru.faizovr.weatherwidget.data.network.GlideApp
import ru.faizovr.weatherwidget.data.network.WeatherResponseCallback
import ru.faizovr.weatherwidget.data.repository.Repository
import ru.faizovr.weatherwidget.domain.model.WeatherModel
import kotlin.concurrent.thread


class WeatherWidget : AppWidgetProvider() {

    /*
    * Comment for master branch:
    *   - call setUpdateButton() before setSettingsButton()
    *   - remove custom action from manifest (? probably the documentation refers to:
    ACTION_APPWIDGET_UPDATE
    ACTION_APPWIDGET_DELETED
    ACTION_APPWIDGET_ENABLED
    ACTION_APPWIDGET_DISABLED
    ACTION_APPWIDGET_OPTIONS_CHANGED)
    * */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val thisWidget = ComponentName(context, this.javaClass)
        val allWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(thisWidget)
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        setLoadingState(context, allWidgetIds, views) // move to updateWidgetViews
        updateWidgetViews(context, views, allWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) { // return custom action
            val extras: Bundle? = intent.extras
            if (extras != null && context != null) {
                val appWidgetIds: IntArray? =
                    extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    setUpdateButton(context, appWidgetIds)
                    Log.d("TAGæ", "onReceive: before onUpdate")
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
                }
            }
        }
    }

    private fun updateWidgetViews(context: Context, views: RemoteViews, appWidgetIds: IntArray) {
        val repository: Repository by lazy { Repository() }
        repository.loadCurrentWeather(object : WeatherResponseCallback {
            override fun onSuccess(weatherModel: WeatherModel) {
                setDataToWidgetViews(context, views, appWidgetIds, weatherModel)
                Log.d("TAGæ", "onSuccess: after setDataToWidgetViews")
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
        val target = AppWidgetTarget(
            context.applicationContext,
            R.id.image_weather,
            views,
            appWidgetIds[0]
        )
        GlideApp.with(context)
            .asBitmap()
            .load(weatherModel.iconUrl)
            .error(R.drawable.icon_weather_cloudy) // change to appropriate error icon
            .into(target) // resolve updating all icons every time
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
        refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE // return custom action
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
}
