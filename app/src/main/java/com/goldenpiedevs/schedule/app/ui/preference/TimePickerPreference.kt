package com.goldenpiedevs.schedule.app.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.goldenpiedevs.schedule.app.R

class TimePickerPreference(context : Context, attr: AttributeSet?, defStyleAttr : Int, defStyleRef : Int) : DialogPreference(context, attr, defStyleAttr, defStyleRef) {

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attr: AttributeSet?) : this (context, attr, 0, R.style.Preference)

}