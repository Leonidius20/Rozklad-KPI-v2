package com.goldenpiedevs.schedule.app.ui.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.utils.work.ShowAlarmWork
import com.goldenpiedevs.schedule.app.ui.alarm.AlarmActivity
import kotlinx.android.synthetic.main.alarm_test_activity_layout.*
import java.util.concurrent.TimeUnit

class AlarmTestActivity : Activity() {

    private var jobId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_test_activity_layout)
    }

    fun startNow(view: View) {
        startActivity(Intent(this, AlarmActivity::class.java).apply {
            putExtra(ShowAlarmWork.LESSON_NUMBER, 1)
        })
    }

    fun schedule(view: View) {
        val TAG = "ShowAlarmWork"
        val DAY_UUID = "day_uuid"
        val LESSON_NUMBER = "lesson_number"

        val dataBuilder = PersistableBundleCompat().apply {
            putString(DAY_UUID, "fff")
            putInt(LESSON_NUMBER, 1)
        }

        jobId = JobRequest.Builder(TAG)
                .setExtras(dataBuilder)
                .setPeriodic(
                        TimeUnit.MINUTES.toMillis(periodTextView.text.toString().toLong()),
                        TimeUnit.MINUTES.toMillis(flexMSTextView.text.toString().toLong()))
                // .setPersisted(true)
                .build()
                .schedule()

    }

    fun cancel(view: View) {
        JobManager.instance().cancel(jobId)
    }


}