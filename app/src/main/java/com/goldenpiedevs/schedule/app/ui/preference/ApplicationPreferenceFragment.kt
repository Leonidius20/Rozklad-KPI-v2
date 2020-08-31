package com.goldenpiedevs.schedule.app.ui.preference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.preference.*
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.ScheduleApplication
import com.goldenpiedevs.schedule.app.core.alarm.manager.AlarmManager
import com.goldenpiedevs.schedule.app.core.api.lessons.LessonsManager
import com.goldenpiedevs.schedule.app.core.api.teachers.TeachersManager
import com.goldenpiedevs.schedule.app.core.dao.timetable.DaoLessonModel
import com.goldenpiedevs.schedule.app.core.notifications.manger.NotificationManager
import com.goldenpiedevs.schedule.app.core.utils.preference.AppPreference
import com.goldenpiedevs.schedule.app.core.utils.preference.UserPreference
import com.goldenpiedevs.schedule.app.core.utils.util.isNetworkAvailable
import com.goldenpiedevs.schedule.app.core.utils.util.restartApp
import com.goldenpiedevs.schedule.app.ui.choose.group.ChooseGroupActivity
import com.goldenpiedevs.schedule.app.ui.widget.ScheduleWidgetProvider
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.text.DateFormat
import java.util.*
import javax.inject.Inject


class ApplicationPreferenceFragment : PreferenceFragmentCompat(), DialogPreference.TargetFragment {

    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var lessonsManager: LessonsManager
    @Inject
    lateinit var teachersManager: TeachersManager
    @Inject
    lateinit var alarmManager: AlarmManager

    companion object {
        const val CHANGE_GROUP_CODE = 565
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        (activity?.applicationContext as ScheduleApplication).appComponent.inject(this)

        preferenceManager.sharedPreferencesName = getString(R.string.user_preference_file_name)
        addPreferencesFromResource(R.xml.app_preference)

        findPreference<ListPreference>(getString(R.string.user_preference_notification_delay_key))!!.apply {
            summary = "${UserPreference.notificationDelay} ${getString(R.string.min)}"

            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                UserPreference.notificationDelay = value.toString()
                summary = "${UserPreference.notificationDelay} ${getString(R.string.min)}"

                notificationManager.createNotification(DaoLessonModel.getLessonsForGroup(AppPreference.groupId))
                true
            }
        }

        findPreference<Preference>(getString(R.string.change_group_key))!!.apply {
            setOnPreferenceClickListener {
                startActivityForResult(Intent(context, ChooseGroupActivity::class.java), CHANGE_GROUP_CODE)
                true
            }
        }

        findPreference<SwitchPreference>(getString(R.string.user_preference_reverse_week_key))!!.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                UserPreference.reverseWeek = value.toString().toBoolean()
                ScheduleWidgetProvider.updateWidget(context!!)

                Handler().postDelayed({
                    context!!.restartApp()
                }, 200)
                true
            }
        }

        findPreference<Preference>(getString(R.string.update_timetable_key))!!.apply {
            setOnPreferenceClickListener {

                if (!context.isNetworkAvailable()) {
                    context.toast(R.string.no_internet)
                    return@setOnPreferenceClickListener true
                }

                val dialog = activity?.indeterminateProgressDialog("Оновлення розкладу")

                GlobalScope.launch {
                    lessonsManager.loadTimeTableAsync(AppPreference.groupId).await()
                    teachersManager.loadTeachersAsync(AppPreference.groupId).await()

                    launch(Dispatchers.Main) {
                        dialog?.dismiss()
                        ScheduleWidgetProvider.updateWidget(context)
                    }
                }
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.user_preference_alarm_switch))!!.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                if (value as Boolean) {
                    alarmManager.scheduleAllAlarms(AppPreference.groupName)
                } else alarmManager.cancelAlarmClocks()
                return@OnPreferenceChangeListener true
            }
        }

        applySummaryAndListener(findPreference(getString(R.string.user_preference_first_lesson_time))!!)
        applySummaryAndListener(findPreference(getString(R.string.user_preference_second_lesson_time))!!)
        applySummaryAndListener(findPreference(getString(R.string.user_preference_third_lesson_time))!!)
        applySummaryAndListener(findPreference(getString(R.string.user_preference_fourth_lesson_time))!!)
    }

    // Applies summaries and OnClickListeners to alarm clock's time preferences
    private fun applySummaryAndListener(pref: Preference) {
        pref.apply {
            var currentStoredTime = UserPreference.preferences.getInt(pref.key, 420)

            summary = formatTime(currentStoredTime)

            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val hours = currentStoredTime / 60
                val minutes = currentStoredTime % 60
                TimePickerDialog.newInstance({ _, hourOfDay, minute, _ ->
                    val time = hourOfDay * 60 + minute
                    if (currentStoredTime != time) {
                        UserPreference.preferences.edit().putInt(pref.key, time).apply()
                        alarmManager.rescheduleAlarmClocks(pref.key)
                    }

                    summary = formatTime(time)
                    currentStoredTime = time // updating for future launches of dialog
                }, hours, minutes, true).show(childFragmentManager, "TimePickerDialog")
                return@OnPreferenceClickListener true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode == Activity.RESULT_OK) {
            true -> {
                when (requestCode) {
                    CHANGE_GROUP_CODE -> {
                        activity?.finish()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Takes time in minutes and outputs time in HH:mm format
     */
    private fun formatTime(timeInMinutes: Int) : String {
        val hours : Int = timeInMinutes / 60
        val minutes : Int = timeInMinutes % 60
        val calendar : Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(calendar.time)
    }
}