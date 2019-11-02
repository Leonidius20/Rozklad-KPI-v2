package com.goldenpiedevs.schedule.app.core.notifications.manger

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.evernote.android.job.JobManager
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.dao.timetable.DaoDayModel
import com.goldenpiedevs.schedule.app.core.dao.timetable.DaoLessonModel
import com.goldenpiedevs.schedule.app.core.dao.timetable.dateFormat
import com.goldenpiedevs.schedule.app.core.dao.timetable.getDayDate
import com.goldenpiedevs.schedule.app.core.utils.preference.AppPreference
import com.goldenpiedevs.schedule.app.core.utils.preference.UserPreference
import com.goldenpiedevs.schedule.app.core.utils.work.ShowAlarmWork
import com.goldenpiedevs.schedule.app.core.utils.work.ShowNotificationWork
import com.goldenpiedevs.schedule.app.ui.lesson.LessonImplementation.Companion.LESSON_ID
import com.goldenpiedevs.schedule.app.ui.main.MainActivity
import io.realm.Realm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit


class NotificationManager(private val context: Context) {

    companion object {
        const val TAG = "notification_job"
    }

    fun createNotification(lessons: List<DaoLessonModel>) {
        GlobalScope.launch {

            val realm = Realm.getDefaultInstance()

            for (lesson in lessons) {
                val time = LocalTime.parse(lesson.timeStart, DateTimeFormatter.ofPattern("HH:mm"))
                val date = LocalDate.parse(lesson.getDayDate(), dateFormat)
                val lessonDateTime = LocalDateTime.of(date, time)
                        .minusMinutes(UserPreference.notificationDelay.toLong())

                var timeToNotify = ChronoUnit.MILLIS.between(LocalDateTime.now(), lessonDateTime)
                if (timeToNotify < 0)
                    timeToNotify += TimeUnit.DAYS.toMillis(14)

                if (lesson.notificationId != -1)
                    JobManager.instance().cancel(lesson.notificationId)

                lesson.notificationId = ShowNotificationWork.enqueueWork(lesson.id, timeToNotify)
            }

            realm.executeTransaction {
                it.copyToRealmOrUpdate(lessons)
            }

            if (!realm.isClosed)
                realm.close()
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("InvalidWakeLockTag")
    fun showNotification(lessonId: String) {
        ShowNotificationWork.enqueueWork(lessonId, TimeUnit.DAYS.toMillis(14)) //repeat notification in 14 days

        val lessonModel = DaoLessonModel.getUniqueLesson(lessonId)

        if (!lessonModel.showNotification ||
                lessonModel.groupId != AppPreference.groupId.toString() ||
                !UserPreference.showNotification)
            return

        val builder: NotificationCompat.Builder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, "notify_001")
        } else {
            NotificationCompat.Builder(context)
        }

        // Setting category
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_ALARM)
        }

        // Creating content intent
        val contentIntent = Intent(context, MainActivity::class.java).putExtra(LESSON_ID, lessonId)

        val startIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(),
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Adding it to builder
        builder.apply {
            setSmallIcon(R.drawable.kpilogo_shields)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setVisibility(Notification.VISIBILITY_PUBLIC)
            priority = Notification.PRIORITY_MAX
            setAutoCancel(true)
            setContentIntent(startIntent)
            setContentTitle("${context.getString(R.string.next_lesson)} ${lessonModel.lessonRoom}")

            setStyle(NotificationCompat.BigTextStyle().bigText(lessonModel.lessonName))
            setContentText(lessonModel.lessonName)
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE, TAG)

        wakeLock?.acquire(500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("notify_001",
                    context.getString(R.string.app_name),
                    IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        NotificationManagerCompat.from(context)
                .notify(lessonModel.lessonId, builder.build())

        wakeLock?.release()
    }

    fun createAlarmClocks(firstLesson: DaoLessonModel, dayId: String) : Int {
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

    private fun scheduleAlarmClock(firstLesson: DaoLessonModel, dayId : String) : Int  {
        val timeToRing = when(firstLesson.lessonNumber) {
            1 -> UserPreference.firstLessonTime
            2 -> UserPreference.secondLessonTime
            3 -> UserPreference.thirdLessonTime
            4 -> UserPreference.fourthLessonTime
            else -> return -1
        }

        val date = LocalDate.parse(firstLesson.getDayDate(), dateFormat)
        val hours : Int = timeToRing / 60
        val minutes : Int = timeToRing % 60
        val alarmDateTime = LocalDateTime.of(date, LocalTime.of(hours, minutes))

        var timeRemaining = ChronoUnit.MILLIS.between(LocalDateTime.now(), alarmDateTime)
        if (timeRemaining < 0)
            timeRemaining += TimeUnit.DAYS.toMillis(14)

        return ShowAlarmWork.enqueueWork(dayId, timeRemaining, firstLesson.lessonNumber)
    }

    fun rescheduleAlarmClocks(lessonNumberKey : String) {
        GlobalScope.launch {
            val lessonNumber = when (lessonNumberKey) {
                context.getString(R.string.user_preference_first_lesson_time) -> 1
                context.getString(R.string.user_preference_second_lesson_time) -> 2
                context.getString(R.string.user_preference_third_lesson_time) -> 3
                context.getString(R.string.user_preference_fourth_lesson_time) -> 4
                else -> return@launch
            }

            val realm = Realm.getDefaultInstance()
            realm.where(DaoDayModel::class.java).equalTo("parentGroup", AppPreference.groupName)
                    .findAll()
                    .also { results ->
                        results.forEach {
                            if (it.lessons.first()?.lessonNumber == lessonNumber) {
                                if (it.alarmClockId != -1)
                                    JobManager.instance().cancel(it.alarmClockId)
                                if (it.lessons.first() != null) {
                                    realm.executeTransaction { realm ->
                                        it.alarmClockId = scheduleAlarmClock(it.lessons.first()!!, it.uuid)
                                        realm.copyToRealmOrUpdate(it)
                                    }
                                }
                            }
                        }
                    }
        }
    }
}