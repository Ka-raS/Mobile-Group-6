package com.group6.group6application.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.StudySchedule
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    internal const val WORK_TAG = "study-notifications"
    internal const val KEY_ID = "schedule_id"
    internal const val KEY_STUDENT = "student_id"
    internal const val KEY_KIND = "kind"
    internal const val KEY_START = "start_millis"
    internal const val REMINDER = "reminder"
    internal const val CHANGED = "changed"
    internal const val DELETED = "deleted"

    internal fun reminderWorkName(id: Int) = "study-reminder-$id"
    internal fun changeWorkName(id: Int) = "study-change-$id"
    internal fun scheduleTag(id: Int) = "study-schedule-$id"

    /** Call after inserting into Room, using the generated schedule ID. */
    fun schedule(context: Context, schedule: StudySchedule): Operation {
        require(schedule.id > 0 && schedule.studentId > 0) { "Save the schedule in Room before scheduling a reminder." }
        val delay = reminderDelayMillis(schedule) ?: return cancel(context, schedule.id)
        val startMillis = studyStart(schedule, ZoneId.systemDefault())!!.toEpochMilli()
        if (StudyNotifications.wasReminded(context, schedule.id, startMillis)) {
            return WorkManager.getInstance(context.applicationContext).cancelUniqueWork(reminderWorkName(schedule.id))
        }
        StudyNotifications.createChannel(context)
        StudyNotifications.dismissReminder(context, schedule.id)
        val input = baseInput(schedule, REMINDER).putLong(KEY_START, startMillis).build()
        val request = OneTimeWorkRequestBuilder<StudyNotificationWorker>()
            .setInputData(input).setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG).addTag(scheduleTag(schedule.id)).build()
        return WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(reminderWorkName(schedule.id), ExistingWorkPolicy.REPLACE, request)
    }

    /** Cancels queued jobs and removes visible notifications for this schedule. */
    fun cancel(context: Context, scheduleId: Int): Operation {
        require(scheduleId > 0)
        StudyNotifications.dismiss(context, scheduleId)
        return WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(scheduleTag(scheduleId))
    }

    /** Call after editing a schedule or updating its status in Room. */
    fun notifyChanged(context: Context, schedule: StudySchedule): Operation {
        StudyNotifications.dismissReminder(context, schedule.id)
        schedule(context, schedule)
        return enqueueChange(context, schedule, baseInput(schedule, CHANGED).build())
    }

    /** Call after deleting, retaining the old row to describe the removed session. */
    fun notifyDeleted(context: Context, deletedSchedule: StudySchedule): Operation {
        cancel(context, deletedSchedule.id)
        val input = baseInput(deletedSchedule, DELETED)
            .putInt("subject_id", deletedSchedule.subjectId)
            .putString("study_date", deletedSchedule.studyDate.take(10))
            .putString("start_time", deletedSchedule.startTime.take(5))
            .putString("end_time", deletedSchedule.endTime.take(5))
            .putString("location", deletedSchedule.location.take(200)).build()
        return enqueueChange(context, deletedSchedule, input)
    }

    fun cancelAll(context: Context): Operation {
        StudyNotifications.dismissAll(context)
        return WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(WORK_TAG)
    }

    /** Restore future reminders after login/resume or after permission is granted. */
    suspend fun restore(context: Context, studentId: Int) {
        val session = SessionManager(context)
        if (!session.isLoggedIn || session.role != Role.STUDENT || session.studentId != studentId) return
        AppDB.get(context).studyScheduleDao().getByStudent(studentId).first().forEach {
            if (reminderDelayMillis(it.schedule) != null) schedule(context, it.schedule)
        }
    }

    private fun baseInput(schedule: StudySchedule, kind: String) = Data.Builder()
        .putInt(KEY_ID, schedule.id).putInt(KEY_STUDENT, schedule.studentId).putString(KEY_KIND, kind)

    private fun enqueueChange(context: Context, schedule: StudySchedule, input: Data): Operation {
        val request = OneTimeWorkRequestBuilder<StudyNotificationWorker>()
            .setInputData(input).addTag(WORK_TAG).addTag(scheduleTag(schedule.id)).build()
        return WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(changeWorkName(schedule.id), ExistingWorkPolicy.REPLACE, request)
    }
}
