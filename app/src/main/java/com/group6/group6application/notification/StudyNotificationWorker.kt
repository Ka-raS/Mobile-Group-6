package com.group6.group6application.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.ScheduleStatus
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException

class StudyNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val studentId = inputData.getInt(NotificationScheduler.KEY_STUDENT, -1)
        val id = inputData.getInt(NotificationScheduler.KEY_ID, -1)
        if (id <= 0 || !isCurrentStudent(applicationContext, studentId)) return Result.success()
        return try {
            val reminder = inputData.getString(NotificationScheduler.KEY_KIND) == NotificationScheduler.REMINDER
            val startMillis = inputData.getLong(NotificationScheduler.KEY_START, -1)
            if (reminder && StudyNotifications.wasReminded(applicationContext, id, startMillis)) return Result.success()
            val message = loadStudyNotification(AppDB.get(applicationContext), inputData) ?: return Result.success()
            if (!isStopped && isCurrentStudent(applicationContext, studentId)) {
                val sent = StudyNotifications.show(applicationContext, id, message, reminder)
                if (sent && reminder) StudyNotifications.markReminded(applicationContext, id, startMillis)
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("StudyNotification", "Không thể tải lịch học $id", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

internal fun isCurrentStudent(context: Context, studentId: Int): Boolean = SessionManager(context).let {
    it.isLoggedIn && it.role == Role.STUDENT && it.studentId == studentId && studentId > 0
}

internal suspend fun loadStudyNotification(
    db: AppDB,
    input: Data,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): StudyNotification? {
    val id = input.getInt(NotificationScheduler.KEY_ID, -1)
    val studentId = input.getInt(NotificationScheduler.KEY_STUDENT, -1)
    if (id <= 0 || studentId <= 0) return null
    val kind = input.getString(NotificationScheduler.KEY_KIND)
    val schedule = db.studyScheduleDao().getById(id)
    if (kind == NotificationScheduler.DELETED) {
        if (schedule != null) return null
        val subjectId = input.getInt("subject_id", -1)
        val subjectName = db.subjectDao().getById(subjectId)?.subjectName ?: "Môn học #$subjectId"
        return StudyNotification(
            "Đã xóa lịch học",
            "$subjectName · ${input.getString("study_date")} · " +
                "${input.getString("start_time")}–${input.getString("end_time")} · ${input.getString("location")}",
        )
    }
    if (schedule == null || schedule.studentId != studentId) return null
    val subject = db.subjectDao().getById(schedule.subjectId) ?: return null
    val title = when (kind) {
        NotificationScheduler.REMINDER -> {
            val start = studyStart(schedule, zone) ?: return null
            if (reminderDelayMillis(schedule, now, zone) == null ||
                start.toEpochMilli() != input.getLong(NotificationScheduler.KEY_START, -1)
            ) return null
            if (start.minusSeconds(REMINDER_MINUTES * 60).isAfter(now)) return null
            "Sắp đến giờ học: ${subject.subjectName}"
        }
        NotificationScheduler.CHANGED -> if (schedule.status == ScheduleStatus.CANCELLED) {
            "Đã hủy lịch học"
        } else {
            "Lịch học đã thay đổi"
        }
        else -> return null
    }
    return StudyNotification(
        title,
        "${subject.subjectName} (${subject.subjectCode}) · ${schedule.studyDate} · " +
            "${schedule.startTime}–${schedule.endTime} · ${schedule.location} · ${ScheduleStatus.label(schedule.status)}",
    )
}
