package com.group6.group6application.activities.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarDatesTest {
    @Test
    fun dayRangeDoesNotIncludeOtherDays() {
        val date = LocalDate.of(2026, 10, 31)
        assertEquals(CalendarRange(date, date), calendarRange(date, CalendarMode.DAY))
    }

    @Test
    fun weekCrossingNewYearStartsOnMondayAndEndsOnSunday() {
        assertEquals(
            CalendarRange(LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 3)),
            calendarRange(LocalDate.of(2027, 1, 1), CalendarMode.WEEK),
        )
        assertEquals(LocalDate.of(2026, 10, 26), weekStart(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun leapFebruaryIncludesLeapDayAndAdjacentMonthCells() {
        val days = monthDays(LocalDate.of(2024, 2, 15))
        assertEquals(LocalDate.of(2024, 1, 29), days.first())
        assertEquals(LocalDate.of(2024, 3, 3), days.last())
        assertTrue(LocalDate.of(2024, 2, 29) in days)
        assertEquals(35, days.size)
    }

    @Test
    fun monthStartingOnSundayCanRequireSixRows() {
        val date = LocalDate.of(2026, 3, 15)
        val days = monthDays(date)
        assertEquals(42, days.size)
        assertEquals(DayOfWeek.MONDAY, days.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, days.last().dayOfWeek)
        assertEquals(CalendarRange(days.first(), days.last()), calendarRange(date, CalendarMode.MONTH))
        assertEquals(31, days.count { it.month == date.month })
        assertTrue(days.zipWithNext().all { (a, b) -> b == a.plusDays(1) })
    }

    @Test
    fun monthNavigationHandlesShortMonthsWithoutInvalidDates() {
        assertEquals(
            LocalDate.of(2024, 2, 29),
            moveCalendarDate(LocalDate.of(2024, 1, 31), CalendarMode.MONTH, 1),
        )
        assertEquals(
            LocalDate.of(2026, 2, 28),
            moveCalendarDate(LocalDate.of(2026, 3, 31), CalendarMode.MONTH, -1),
        )
    }

    @Test
    fun dayAndWeekNavigationCrossYearBoundaries() {
        assertEquals(
            LocalDate.of(2027, 1, 1),
            moveCalendarDate(LocalDate.of(2026, 12, 31), CalendarMode.DAY, 1),
        )
        assertEquals(
            LocalDate.of(2026, 12, 25),
            moveCalendarDate(LocalDate.of(2027, 1, 1), CalendarMode.WEEK, -1),
        )
    }
}
