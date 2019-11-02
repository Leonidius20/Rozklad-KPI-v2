package com.goldenpiedevs.schedule.app.core.utils.work

import android.content.Intent
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.goldenpiedevs.schedule.app.core.utils.preference.UserPreference
import com.goldenpiedevs.schedule.app.ui.alarm.AlarmActivity
import java.util.concurrent.TimeUnit

class ShowAlarmWork : Job() {
    override fun onRunJob(p0: Params): Result {
        if (UserPreference.alarmSwitch) {
            context.startActivity(Intent(context, AlarmActivity::class.java))
        }
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "ShowAlarmWork"
        private const val DAY_UUID = "day_uuid"

        fun enqueueWork(dayId: String, timeRemaining: Long): Int {
            val dataBuilder = PersistableBundleCompat().apply {
                putString(DAY_UUID, dayId)
            }
            return JobRequest.Builder(TAG)
                    .setExtras(dataBuilder)
                    .setPeriodic(TimeUnit.DAYS.toMillis(14), timeRemaining)
                    .build()
                    .schedule()
        }
    }
}