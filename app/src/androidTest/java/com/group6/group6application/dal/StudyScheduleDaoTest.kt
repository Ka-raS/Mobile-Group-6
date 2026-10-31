package com.group6.group6application.dal

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.Student
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.entities.Subject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyScheduleDaoTest {
    private lateinit var database: AppDB
    private lateinit var scheduleDao: StudyScheduleDao

    @Before
    fun createDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDB::class.java)
            .allowMainThreadQueries()
            .build()
        scheduleDao = database.studyScheduleDao()

        database.accountDao().insert(Account(1, "student-one", "password", Role.STUDENT))
        database.accountDao().insert(Account(2, "student-two", "password", Role.STUDENT))
        database.studentDao().insert(Student(1, "S001", "Student One", "one@example.com", "1", 1))
        database.studentDao().insert(Student(2, "S002", "Student Two", "two@example.com", "2", 2))
        database.subjectDao().insert(Subject(1, "MOB101", "Mobile Programming", "", 3))
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun partialAndContainedTimeRangesOverlap() = runBlocking {
        insertSchedule(start = "09:00", end = "11:00")

        assertEquals(1, overlap(start = "08:00", end = "10:00"))
        assertEquals(1, overlap(start = "10:00", end = "12:00"))
        assertEquals(1, overlap(start = "09:30", end = "10:30"))
        assertEquals(1, overlap(start = "08:00", end = "12:00"))
    }

    @Test
    fun adjacentTimeRangesDoNotOverlap() = runBlocking {
        insertSchedule(start = "09:00", end = "11:00")

        assertEquals(0, overlap(start = "08:00", end = "09:00"))
        assertEquals(0, overlap(start = "11:00", end = "12:00"))
    }

    @Test
    fun differentDateOrStudentDoesNotOverlap() = runBlocking {
        insertSchedule(start = "09:00", end = "11:00")

        assertEquals(0, overlap(date = "2026-11-02", start = "09:30", end = "10:30"))
        assertEquals(0, overlap(studentId = 2, start = "09:30", end = "10:30"))
    }

    @Test
    fun cancelledScheduleDoesNotBlockBooking() = runBlocking {
        insertSchedule(start = "09:00", end = "11:00", status = ScheduleStatus.CANCELLED)

        assertEquals(0, overlap(start = "09:30", end = "10:30"))
    }

    @Test
    fun excludeIdIgnoresEditedScheduleButNotAnotherSchedule() = runBlocking {
        val editedId = insertSchedule(start = "09:00", end = "10:00")
        insertSchedule(start = "11:00", end = "12:00")

        assertEquals(0, overlap(start = "09:00", end = "10:00", excludeId = editedId))
        assertEquals(1, overlap(start = "11:30", end = "12:30", excludeId = editedId))
    }

    private suspend fun insertSchedule(
        studentId: Int = 1,
        date: String = "2026-11-01",
        start: String,
        end: String,
        status: String = ScheduleStatus.PENDING,
    ): Int = scheduleDao.insert(
        StudySchedule(
            studentId = studentId,
            subjectId = 1,
            studyDate = date,
            startTime = start,
            endTime = end,
            location = "Room 101",
            status = status,
        )
    ).toInt()

    private suspend fun overlap(
        studentId: Int = 1,
        date: String = "2026-11-01",
        start: String,
        end: String,
        excludeId: Int = 0,
    ): Int = scheduleDao.countOverlap(studentId, date, start, end, excludeId)
}
