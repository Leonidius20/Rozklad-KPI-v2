package com.goldenpiedevs.schedule.app.ui.main

import com.goldenpiedevs.schedule.app.ui.base.BasePresenter
import com.google.android.material.navigation.NavigationView
import java.util.*

interface MainPresenter : BasePresenter<MainView> {
    fun setSupportFragmentManager(supportFragmentManager: androidx.fragment.app.FragmentManager)
    fun setNavigationView(navigationView: NavigationView)

    fun showCurrentDayTitle()
    fun showTimeTable()
    fun onMapClick()
    fun onGroupChangeClick()
    fun onSettingsClick()
    fun onTeachersClick()
    fun onTimeTableClick()
    fun updateCalendarState()
    fun onCalendarOpen(firstDayOfNewMonth: Date)
    fun checkItem()
}