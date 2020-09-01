package com.goldenpiedevs.schedule.app.core.utils.work

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.goldenpiedevs.schedule.app.ScheduleApplication
import com.goldenpiedevs.schedule.app.core.alarm.manager.AlarmManager
import javax.inject.Inject

class ShowAlarmWork : Job() {

    @Inject
    lateinit var alarmManager: AlarmManager

    override fun onRunJob(params: Params): Result {
        (context.applicationContext as ScheduleApplication).appComponent.inject(this)

        alarmManager.showAlarmScreen(params.extras.getInt(LESSON_NUMBER, -1))

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "ShowAlarmWork"
        const val LESSON_NUMBER = "lesson_number"

        fun enqueueWork(timeRemaining: Long, lessonNumber: Int): Int {
            val dataBuilder = PersistableBundleCompat().apply {
                putInt(LESSON_NUMBER, lessonNumber)
            }
            return JobRequest.Builder(TAG)
                    .setExtras(dataBuilder)
                    .setExact(timeRemaining)
                    .build()
                    .schedule()
        }
    }
}