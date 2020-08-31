package com.goldenpiedevs.schedule.app.core.injection.module

import android.content.Context
import com.goldenpiedevs.schedule.app.core.alarm.manager.AlarmManager
import com.goldenpiedevs.schedule.app.core.api.group.GroupManager
import com.goldenpiedevs.schedule.app.core.api.group.GroupService
import com.goldenpiedevs.schedule.app.core.api.lessons.LessonsManager
import com.goldenpiedevs.schedule.app.core.api.lessons.LessonsService
import com.goldenpiedevs.schedule.app.core.api.teachers.TeachersManager
import com.goldenpiedevs.schedule.app.core.api.teachers.TeachersService
import com.goldenpiedevs.schedule.app.core.notifications.manger.NotificationManager
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
object NetworkManagerModule {
    @Provides
    @Reusable
    fun provideGroupManager(groupService: GroupService) = GroupManager(groupService)

    @Provides
    @Reusable
    fun provideTeachersManager(context: Context, teachersService: TeachersService) = TeachersManager(context, teachersService)

    @Provides
    @Reusable
    fun provideLessonsManager(context: Context, lessonsService: LessonsService,
                              groupManager: GroupManager,
                              notificationManager: NotificationManager, alarmManager: AlarmManager)
            = LessonsManager(context, lessonsService, groupManager, notificationManager, alarmManager)
}