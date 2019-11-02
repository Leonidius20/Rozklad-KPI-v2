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
            val intent = Intent(context, AlarmActivity::class.java)
            intent.putExtra(LESSON_NUMBER, p0.extras.getInt(LESSON_NUMBER, -1))
            context.startActivity(intent)
        }
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "ShowAlarmWork"
        private const val DAY_UUID = "day_uuid"
        const val LESSON_NUMBER = "lesson_number"

        fun enqueueWork(dayId: String, timeRemaining: Long, lessonNumber : Int): Int {
            val dataBuilder = PersistableBundleCompat().apply {
                putString(DAY_UUID, dayId)
                putInt(LESSON_NUMBER, lessonNumber)
            }
            return JobRequest.Builder(TAG)
                    .setExtras(dataBuilder)
                    .setPeriodic(TimeUnit.DAYS.toMillis(14), timeRemaining)
                    .build()
                    .schedule()
        }
    }
}