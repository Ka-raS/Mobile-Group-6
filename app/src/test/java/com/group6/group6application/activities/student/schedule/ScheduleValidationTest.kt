package com.group6.group6application.activities.student.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleValidationTest {
    @Test
    fun formatDateAndTime_zeroPadsEveryComponent() {
        assertEquals("2026-01-02", formatScheduleDate(2026, 1, 2))
        assertEquals("09:05", formatScheduleTime(9, 5))
    }

    @Test
    fun dateValidation_requiresStrictRealIsoDate() {
        assertTrue(isValidScheduleDate("2028-02-29"))
        assertFalse(isValidScheduleDate("2027-02-29"))
        assertFalse(isValidScheduleDate("2026-2-03"))
        assertFalse(isValidScheduleDate("03-02-2026"))
        assertFalse(isValidScheduleDate("2026-04-31"))
    }

    @Test
    fun timeValidation_requiresStrict24HourTime() {
        assertTrue(isValidScheduleTime("00:00"))
        assertTrue(isValidScheduleTime("23:59"))
        assertFalse(isValidScheduleTime("9:05"))
        assertFalse(isValidScheduleTime("24:00"))
        assertFalse(isValidScheduleTime("12:60"))
    }

    @Test
    fun validation_marksEveryRequiredField() {
        val result = validateScheduleForm(
            subjectId = null,
            studyDate = "",
            startTime = "",
            endTime = "",
            location = "   ",
        )

        assertFalse(result.isValid)
        assertNull(result.fields)
        assertNotNull(result.errors.subject)
        assertNotNull(result.errors.studyDate)
        assertNotNull(result.errors.startTime)
        assertNotNull(result.errors.endTime)
        assertNotNull(result.errors.location)
    }

    @Test
    fun validation_rejectsEqualOrReversedTimes() {
        val equal = validInput(startTime = "10:00", endTime = "10:00")
        val reversed = validInput(startTime = "10:01", endTime = "10:00")

        assertFalse(equal.isValid)
        assertEquals("End time must be after start time.", equal.errors.endTime)
        assertFalse(reversed.isValid)
        assertEquals("End time must be after start time.", reversed.errors.endTime)
    }

    @Test
    fun validation_rejectsNonPaddedValues() {
        val result = validateScheduleForm(
            subjectId = 1,
            studyDate = "2026-1-02",
            startTime = "9:00",
            endTime = "10:00",
            location = "Library",
        )

        assertFalse(result.isValid)
        assertNotNull(result.errors.studyDate)
        assertNotNull(result.errors.startTime)
    }

    @Test
    fun validation_returnsNormalizedFieldsForValidInput() {
        val result = validInput(location = "  Main Library  ")

        assertTrue(result.isValid)
        val fields = requireNotNull(result.fields)
        assertEquals(1, fields.subjectId)
        assertEquals("2026-10-31", fields.studyDate)
        assertEquals("09:00", fields.startTime)
        assertEquals("10:00", fields.endTime)
        assertEquals("Main Library", fields.location)
        assertFalse(result.errors.hasErrors)
    }

    private fun validInput(
        startTime: String = "09:00",
        endTime: String = "10:00",
        location: String = "Main Library",
    ): ScheduleValidationResult = validateScheduleForm(
        subjectId = 1,
        studyDate = "2026-10-31",
        startTime = startTime,
        endTime = endTime,
        location = location,
    )
}
