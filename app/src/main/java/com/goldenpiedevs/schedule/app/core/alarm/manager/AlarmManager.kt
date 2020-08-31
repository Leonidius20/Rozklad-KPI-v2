package com.goldenpiedevs.schedule.app.core.alarm.manager

import android.content.Context
import com.evernote.android.job.JobManager
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.dao.group.DaoGroupModel
import com.goldenpiedevs.schedule.app.core.dao.timetable.DaoDayModel
import com.goldenpiedevs.schedule.app.core.dao.timetable.DaoLessonModel
import com.goldenpiedevs.schedule.app.core.dao.timetable.dateFormat
import com.goldenpiedevs.schedule.app.core.dao.timetable.getDayDate
import com.goldenpiedevs.schedule.app.core.utils.preference.AppPreference
import com.goldenpiedevs.schedule.app.core.utils.preference.UserPreference
import com.goldenpiedevs.schedule.app.core.utils.work.ShowAlarmWork
import io.realm.Realm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Creates and manages automatic alarm clocks
 */
class AlarmManager(private val context: Context) {

    /**
     * Creates an alarm clock for a specified day
     * @see com.goldenpiedevs.schedule.app.core.dao.timetable.DaoDayModel.saveGroupTimeTable
     */
    fun createAlarmClock(firstLesson: DaoLessonModel, dayId: String): Int {
        /*val realm = Realm.getDefaultInstance()
        if (day.lessons.first() != null) {
            val jobId = scheduleAlarmClock(day.lessons.first()!!, day.uuid)
            realm.executeTransaction {
                day.alarmClockId = jobId
                it.copyToRealmOrUpdate(day)
            }
        }*/
        return scheduleAlarmClock(firstLesson, dayId)
    }

    private fun scheduleAlarmClock(firstLesson: DaoLessonModel, dayId: String): Int {
        val timeToRing = when (firstLesson.lessonNumber) {
            1 -> UserPreference.firstLessonTime
            2 -> UserPreference.secondLessonTime
            3 -> UserPreference.thirdLessonTime
            4 -> UserPreference.fourthLessonTime
            else -> return -1
        }

        val date = LocalDate.parse(firstLesson.getDayDate(), dateFormat)
        val hours: Int = timeToRing / 60
        val minutes: Int = timeToRing % 60
        val alarmDateTime = LocalDateTime.of(date, LocalTime.of(hours, minutes))

        var timeRemaining = ChronoUnit.MILLIS.between(LocalDateTime.now(), alarmDateTime)
        if (timeRemaining < 0)
            timeRemaining += TimeUnit.DAYS.toMillis(14)

        return ShowAlarmWork.enqueueWork(dayId, timeRemaining, firstLesson.lessonNumber)
    }

    /**
     * Reschedules all alarm clocks for days, when the first lesson
     * has a set number. Called when alarm time is changed in preferences
     * @see com.goldenpiedevs.schedule.app.ui.preference.ApplicationPreferenceFragment.onCreatePreferences
     */
    fun rescheduleAlarmClocks(lessonNumberKey: String) {
        GlobalScope.launch {
            val lessonNumber = when (lessonNumberKey) {
                context.getString(R.string.user_preference_first_lesson_time) -> 1
                context.getString(R.string.user_preference_second_lesson_time) -> 2
                context.getString(R.string.user_preference_third_lesson_time) -> 3
                context.getString(R.string.user_preference_fourth_lesson_time) -> 4
                else -> return@launch
            }

            val realm = Realm.getDefaultInstance()
            realm.where(DaoDayModel::class.java)
                    .equalTo("parentGroup", AppPreference.groupName)
                    .findAll()
                    .also { results ->
                        results.forEach {
                            val lessons = it.lessons.sortedBy { lessonNumber }
                            if (lessons.first()?.lessonNumber == lessonNumber) {
                                if (it.alarmClockId != -1)
                                    JobManager.instance().cancel(it.alarmClockId)
                                if (lessons.first() != null) {
                                    realm.executeTransaction { realm ->
                                        it.alarmClockId =
                                                scheduleAlarmClock(lessons.first()!!, it.uuid)
                                        realm.copyToRealmOrUpdate(it)
                                    }
                                }
                            }
                        }
                    }
        }
    }

    /**
     * Cancels all scheduled alarm clocks
     * @see com.goldenpiedevs.schedule.app.ui.preference.ApplicationPreferenceFragment.onCreatePreferences
     */
    fun cancelAlarmClocks() {
        GlobalScope.launch {
            Realm.getDefaultInstance()
                    .where(DaoDayModel::class.java).findAll()
                    .also { results ->
                        results.forEach {
                            if (it.alarmClockId != -1) {
                                JobManager.instance().cancel(it.alarmClockId)
                            }
                        }
                    }
        }
    }

    /**
     * Schedules all alarm clocks for a group
     * @see com.goldenpiedevs.schedule.app.ui.preference.ApplicationPreferenceFragment.onCreatePreferences
     */
    fun scheduleAllAlarms(groupName: String) {
        Realm.getDefaultInstance().where(DaoGroupModel::class.java).equalTo(
                "groupFullName", groupName).findFirst()?.let {
            scheduleAllAlarms(DaoLessonModel.getLessonsForGroup(it.groupId), groupName)
        }
    }

    /**
     * Schedules all alarm clocks for a group
     * @see com.goldenpiedevs.schedule.app.core.dao.timetable.DaoDayModel.saveGroupTimeTable
     * @param list List of all lessons for a group
     */
    fun scheduleAllAlarms(list: List<DaoLessonModel>, groupName: String) {
        val realm = Realm.getDefaultInstance()

        list.groupBy { it.lessonWeek }.forEach { (weekNum, weekLessonsList) ->
            weekLessonsList.asSequence().sortedBy { it.lessonNumber }.groupBy { it.dayNumber }.forEach { (dayNum, dayLessonsList) ->
                val dayModel = realm.where(DaoDayModel::class.java)
                        .equalTo("parentGroup", groupName)
                        .equalTo("dayNumber", dayNum)
                        .equalTo("weekNumber", weekNum).findFirst()

                dayModel?.let { modelIt ->
                    realm.executeTransaction {
                        modelIt.alarmClockId =
                                createAlarmClock(dayLessonsList.first(), modelIt.uuid)
                        it.copyToRealmOrUpdate(modelIt)
                    }
                } ?: run {
                    realm.executeTransaction {
                        it.copyToRealmOrUpdate(DaoDayModel().apply {
                            alarmClockId =
                                    createAlarmClock(dayLessonsList.first(), this.uuid)
                        })
                    }
                }

            }
        }
    }

}