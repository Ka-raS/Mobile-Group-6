package com.group6.group6application.activities.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

internal enum class CalendarMode(val label: String) {
    DAY("Ngày"), WEEK("Tuần"), MONTH("Tháng"),
}

internal data class CalendarRange(val start: LocalDate, val end: LocalDate)

internal fun weekStart(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

internal fun monthDays(date: LocalDate): List<LocalDate> {
    val month = YearMonth.from(date)
    val start = weekStart(month.atDay(1))
    val end = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
}

internal fun calendarRange(date: LocalDate, mode: CalendarMode): CalendarRange = when (mode) {
    CalendarMode.DAY -> CalendarRange(date, date)
    CalendarMode.WEEK -> weekStart(date).let { CalendarRange(it, it.plusDays(6)) }
    // Include the adjacent-month cells so their booking counts are also correct.
    CalendarMode.MONTH -> monthDays(date).let { CalendarRange(it.first(), it.last()) }
}

internal fun moveCalendarDate(date: LocalDate, mode: CalendarMode, amount: Long): LocalDate =
    when (mode) {
        CalendarMode.DAY -> date.plusDays(amount)
        CalendarMode.WEEK -> date.plusWeeks(amount)
        CalendarMode.MONTH -> date.plusMonths(amount)
    }
