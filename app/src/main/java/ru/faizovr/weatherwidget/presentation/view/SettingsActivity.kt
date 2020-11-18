package ru.faizovr.weatherwidget.presentation.view

import android.app.Activity
import android.os.Bundle
import ru.faizovr.weatherwidget.R

class SettingsActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)
    }
}