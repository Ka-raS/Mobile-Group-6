package com.group6.group6application.activities.student

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.ScheduleWithSubject
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.entities.Subject
import com.group6.group6application.activities.student.schedule.ScheduleFormFieldErrors
import com.group6.group6application.activities.student.schedule.ScheduleFormActivity
import com.group6.group6application.activities.student.schedule.ScheduleListActivity
import com.group6.group6application.activities.theme.Group6ApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StudentScheduleActivitiesTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun studentHomeShowsMenuAndDispatchesNavigation() {
        var schedulesOpened = false
        var bookingOpened = false

        composeRule.setContent {
            Group6ApplicationTheme {
                StudentHomeActivity(
                    username = "student1",
                    onSchedules = { schedulesOpened = true },
                    onBook = { bookingOpened = true },
                    onCalendar = {},
                    onLogout = {},
                )
            }
        }

        composeRule.onNodeWithText("Xin chào, student1!").assertIsDisplayed()
        composeRule.onNodeWithTag("student_home_schedules_button").performClick()
        composeRule.onNodeWithTag("student_home_book_button").performClick()

        assertTrue(schedulesOpened)
        assertTrue(bookingOpened)
    }

    @Test
    fun scheduleListShowsEmptyStateAndDispatchesSearchAndFilter() {
        var query = ""
        var status: String? = ScheduleStatus.PENDING

        composeRule.setContent {
            Group6ApplicationTheme {
                ScheduleListActivity(
                    schedules = emptyList(),
                    query = "",
                    selectedStatus = null,
                    isBusy = false,
                    errorMessage = null,
                    sessionValid = true,
                    onQueryChange = { query = it },
                    onStatusFilterChange = { status = it },
                    onAdd = {},
                    onEdit = {},
                    onDelete = {},
                    onStatusChange = { _, _ -> },
                    onDismissError = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Chưa có lịch học").assertIsDisplayed()
        composeRule.onNodeWithTag("schedule_search_field").performTextInput("mobile")
        composeRule.onNodeWithTag("schedule_filter_confirmed").performClick()

        assertEquals("mobile", query)
        assertEquals(ScheduleStatus.CONFIRMED, status)
    }

    @Test
    fun scheduleListConfirmsBeforeDeleting() {
        val schedule = sampleSchedule()

        composeRule.setContent {
            Group6ApplicationTheme {
                ScheduleListActivity(
                    schedules = listOf(
                        ScheduleWithSubject(schedule, "MOB101", "Lập trình di động")
                    ),
                    query = "",
                    selectedStatus = null,
                    isBusy = false,
                    errorMessage = null,
                    sessionValid = true,
                    onQueryChange = {},
                    onStatusFilterChange = {},
                    onAdd = {},
                    onEdit = {},
                    onDelete = {},
                    onStatusChange = { _, _ -> },
                    onDismissError = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithTag("schedule_delete_${schedule.id}").performClick()
        composeRule.onNodeWithTag("schedule_delete_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Lịch học này sẽ bị xóa vĩnh viễn.").assertIsDisplayed()
    }

    @Test
    fun scheduleFormDisplaysInlineErrorsAndDispatchesSave() {
        var saveClicked = false

        composeRule.setContent {
            Group6ApplicationTheme {
                ScheduleFormActivity(
                    isEditing = false,
                    subjects = listOf(Subject(1, "MOB101", "Lập trình di động", "", 3)),
                    selectedSubjectId = null,
                    studyDate = "",
                    startTime = "",
                    endTime = "",
                    location = "",
                    status = ScheduleStatus.PENDING,
                    isLoading = false,
                    isSaving = false,
                    errorMessage = null,
                    fieldErrors = ScheduleFormFieldErrors(
                        subject = "Hãy chọn môn học.",
                        studyDate = "Hãy chọn ngày học.",
                        startTime = "Hãy chọn giờ bắt đầu.",
                        endTime = "Hãy chọn giờ kết thúc.",
                        location = "Hãy nhập địa điểm.",
                    ),
                    sessionValid = true,
                    onSubjectChange = {},
                    onDateChange = {},
                    onStartTimeChange = {},
                    onEndTimeChange = {},
                    onLocationChange = {},
                    onSave = { saveClicked = true },
                    onDismissError = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithTag("schedule_form_subject_error", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("schedule_form_date_error", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("schedule_form_location_error", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("schedule_form_save").performClick()

        assertTrue(saveClicked)
    }

    private fun sampleSchedule() = StudySchedule(
        id = 7,
        studentId = 1,
        subjectId = 1,
        studyDate = "2026-11-01",
        startTime = "09:00",
        endTime = "10:00",
        location = "Room 101",
        status = ScheduleStatus.PENDING,
    )
}
