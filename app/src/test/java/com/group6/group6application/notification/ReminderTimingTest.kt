package com.group6.group6application.notification

import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.StudySchedule
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderTimingTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val schedule = StudySchedule(1, 1, 1, "2026-11-01", "10:00", "11:00", "Phòng A")

    @Test
    fun schedulesThirtyMinutesBeforeStartInDeviceTimeZone() {
        assertEquals(5_400_000L, reminderDelayMillis(schedule, Instant.parse("2026-11-01T01:00:00Z"), zone))
        assertEquals(Instant.parse("2026-11-01T03:00:00Z"), studyStart(schedule, zone))
    }

    @Test
    fun bookingWithinThirtyMinutesIsRemindedImmediately() {
        assertEquals(0L, reminderDelayMillis(schedule, Instant.parse("2026-11-01T02:45:00Z"), zone))
        assertEquals(0L, reminderDelayMillis(schedule, Instant.parse("2026-11-01T02:30:00Z"), zone))
    }

    @Test
    fun skipsStartedAndPastSessions() {
        assertNull(reminderDelayMillis(schedule, Instant.parse("2026-11-01T03:00:00Z"), zone))
        assertNull(reminderDelayMillis(schedule, Instant.parse("2026-11-02T01:00:00Z"), zone))
    }

    @Test
    fun skipsCancelledAndUnknownStatuses() {
        val now = Instant.parse("2026-11-01T01:00:00Z")
        assertNull(reminderDelayMillis(schedule.copy(status = ScheduleStatus.CANCELLED), now, zone))
        assertNull(reminderDelayMillis(schedule.copy(status = "Unknown"), now, zone))
        assertEquals(5_400_000L, reminderDelayMillis(schedule.copy(status = ScheduleStatus.CONFIRMED), now, zone))
    }

    @Test
    fun invalidStoredDatesAndTimesDoNotCrashScheduler() {
        val now = Instant.parse("2026-11-01T01:00:00Z")
        assertNull(reminderDelayMillis(schedule.copy(studyDate = "2026-02-30"), now, zone))
        assertNull(reminderDelayMillis(schedule.copy(startTime = "25:00"), now, zone))
        assertNull(reminderDelayMillis(schedule.copy(studyDate = ""), now, zone))
    }

    @Test
    fun reminderCanCrossMidnightAndYearBoundary() {
        val session = schedule.copy(studyDate = "2027-01-01", startTime = "00:15")
        // 23:30 on December 31 in Vietnam; reminder is due at 23:45.
        assertEquals(900_000L, reminderDelayMillis(session, Instant.parse("2026-12-31T16:30:00Z"), zone))
    }
}
