package com.group6.group6application.notification

import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.StudySchedule
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal const val REMINDER_MINUTES = 30L

internal fun studyStart(schedule: StudySchedule, zone: ZoneId): Instant? = try {
    LocalDate.parse(schedule.studyDate).atTime(LocalTime.parse(schedule.startTime)).atZone(zone).toInstant()
} catch (_: DateTimeException) {
    null
}

internal fun reminderDelayMillis(
    schedule: StudySchedule,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): Long? {
    if (schedule.status != ScheduleStatus.PENDING && schedule.status != ScheduleStatus.CONFIRMED) return null
    val start = studyStart(schedule, zone) ?: return null
    if (!start.isAfter(now)) return null
    return Duration.between(now, start.minusSeconds(REMINDER_MINUTES * 60)).toMillis().coerceAtLeast(0)
}
