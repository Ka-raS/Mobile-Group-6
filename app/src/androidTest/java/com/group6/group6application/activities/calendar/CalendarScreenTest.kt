package com.group6.group6application.activities.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.group6.group6application.dal.AppDB
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.Student
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.entities.Subject
import com.group6.group6application.activities.theme.Group6ApplicationTheme
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var db: AppDB
    private lateinit var mySchedule: StudySchedule

    @Before
    fun setUp(): Unit = runBlocking {
        // An isolated database: no changes to the app's real accounts or schedules.
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDB::class.java).build()
        for (id in 1..2) {
            db.accountDao().insert(Account(id, "student$id", "test", Role.STUDENT))
            db.studentDao().insert(Student(id, "SV$id", "Student $id", "s$id@example.com", "090000000$id", id))
        }
        db.subjectDao().insert(Subject(1, "MY101", "Môn của tôi", "", 3))
        db.subjectDao().insert(Subject(2, "OTHER101", "Môn của người khác", "", 3))
        db.subjectDao().insert(Subject(3, "WEEK101", "Buổi khác trong tuần", "", 3))
        val today = LocalDate.now()
        mySchedule = StudySchedule(1, 1, 1, today.toString(), "08:00", "09:00", "Phòng A", ScheduleStatus.CONFIRMED)
        db.studyScheduleDao().insert(mySchedule)
        db.studyScheduleDao().insert(mySchedule.copy(id = 2, studentId = 2, subjectId = 2))
        val otherDay = weekStart(today).let { if (it == today) it.plusDays(6) else it }
        db.studyScheduleDao().insert(
            mySchedule.copy(id = 3, subjectId = 3, studyDate = otherDay.toString(), status = ScheduleStatus.CANCELLED),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun showCalendar() {
        compose.setContent {
            Group6ApplicationTheme(dynamicColor = false) {
                StudentCalendar(studentId = 1, scheduleDao = db.studyScheduleDao(), onBack = {})
            }
        }
    }

    @Test
    fun dailyViewShowsOnlyCurrentStudentAndUpdatesAfterRoomEdit() {
        showCalendar()
        compose.onNodeWithText("Ngày").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Môn của tôi").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("calendar-list").performScrollToNode(hasText("Môn của tôi"))
        compose.onNodeWithText("Môn của tôi").assertIsDisplayed()
        compose.onNodeWithText("Môn của người khác").assertDoesNotExist()
        compose.onNodeWithText("Đã xác nhận").assertIsDisplayed()

        runBlocking { db.studyScheduleDao().update(mySchedule.copy(location = "Phòng mới")) }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Địa điểm: Phòng mới").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Địa điểm: Phòng mới").assertIsDisplayed()
    }

    @Test
    fun canNavigateMonthsAndShowAllDaysOfWeekIncludingCancelledBookings() {
        showCalendar()
        compose.onNodeWithContentDescription("Tháng trước").performClick()
        compose.onNodeWithText("Hôm nay").performClick()
        compose.onNodeWithText("Tuần").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Lịch trong tuần · 2 buổi").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("calendar-list").performScrollToNode(hasText("Buổi khác trong tuần"))
        compose.onNodeWithText("Buổi khác trong tuần").assertIsDisplayed()
        compose.onNodeWithText("Đã hủy").assertIsDisplayed()
        compose.onNodeWithText("Môn của người khác").assertDoesNotExist()
    }
}
