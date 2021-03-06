package com.goldenpiedevs.schedule.app.core.utils.preference

import com.chibatching.kotpref.KotprefModel

object AppPreference : KotprefModel() {
    var isFirstLaunch by booleanPref(true)
    var groupName by stringPref()
    var groupId by intPref()

    var isCalendarOpen by booleanPref(false)

    var lastTimeTableUpdate by longPref(-1)
}