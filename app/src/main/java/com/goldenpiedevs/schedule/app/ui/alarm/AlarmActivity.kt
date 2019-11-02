package com.goldenpiedevs.schedule.app.ui.alarm

import android.media.RingtoneManager
import android.os.Bundle
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.ui.base.BaseActivity

class AlarmActivity : BaseActivity<AlarmPresenter, AlarmView>(), AlarmView {

    private lateinit var presenter: AlarmPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AlarmImplementation()
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone.play()
    }

    override fun getPresenterChild(): AlarmPresenter = presenter

    override fun getActivityLayout(): Int {
        return R.layout.alarm_activity_layout
    }
}