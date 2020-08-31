package com.goldenpiedevs.schedule.app.ui.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.utils.work.ShowAlarmWork
import com.goldenpiedevs.schedule.app.ui.alarm.AlarmActivity

class AlarmTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_test_activity_layout)
    }

    fun startNow(view: View) {
        startActivity(Intent(this, AlarmActivity::class.java).apply {
            putExtra(ShowAlarmWork.LESSON_NUMBER, 1)
        })
    }


}