package com.goldenpiedevs.schedule.app.ui.alarm

import android.media.AudioManager.STREAM_ALARM
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.utils.work.ShowAlarmWork
import com.goldenpiedevs.schedule.app.ui.base.BaseActivity
import kotlinx.android.synthetic.main.alarm_activity_layout.*

class AlarmActivity : BaseActivity<AlarmPresenter, AlarmView>(), AlarmView {

    private lateinit var presenter: AlarmPresenter
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            val lessonNumber = intent.getIntExtra(ShowAlarmWork.LESSON_NUMBER, -1)
            lessonNumberView.text = when(lessonNumber) {
                1 -> "First lesson"
                2 -> "Second lesson"
                3 -> "Third lesson"
                4 -> "Fourth lesson"
                else -> ""
            }
        }

        presenter = AlarmImplementation()
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone.play()

        mediaPlayer = MediaPlayer()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, alarmUri)
        mediaPlayer.setAudioStreamType(STREAM_ALARM)
        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.start()

        stopButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.release()
            finish()
        }
    }

    override fun getPresenterChild(): AlarmPresenter = presenter

    override fun getActivityLayout(): Int {
        return R.layout.alarm_activity_layout
    }
}