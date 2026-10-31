package com.group6.group6application.activities.student.schedule

import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val scheduleDateFormatter = DateTimeFormatter
    .ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT)

private val scheduleTimeFormatter = DateTimeFormatter
    .ofPattern("HH:mm")
    .withResolverStyle(ResolverStyle.STRICT)

private val strictDatePattern = Regex("\\d{4}-(0[1-9]|1[0-2])-([0-2]\\d|3[01])")
private val strictTimePattern = Regex("([01]\\d|2[0-3]):[0-5]\\d")

data class ScheduleFormFieldErrors(
    val subject: String? = null,
    val studyDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
) {
    val hasErrors: Boolean
        get() = subject != null ||
            studyDate != null ||
            startTime != null ||
            endTime != null ||
            location != null
}

data class ValidatedScheduleFields(
    val subjectId: Int,
    val studyDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
)

data class ScheduleValidationResult(
    val fields: ValidatedScheduleFields? = null,
    val errors: ScheduleFormFieldErrors = ScheduleFormFieldErrors(),
) {
    val isValid: Boolean get() = fields != null && !errors.hasErrors
}

fun validateScheduleForm(
    subjectId: Int?,
    studyDate: String,
    startTime: String,
    endTime: String,
    location: String,
): ScheduleValidationResult {
    val normalizedLocation = location.trim()
    val dateError = when {
        studyDate.isBlank() -> "Hãy chọn ngày học."
        !isValidScheduleDate(studyDate) -> "Ngày không hợp lệ, dùng định dạng yyyy-MM-dd."
        else -> null
    }
    val startError = when {
        startTime.isBlank() -> "Hãy chọn giờ bắt đầu."
        !isValidScheduleTime(startTime) -> "Giờ bắt đầu không hợp lệ, dùng định dạng HH:mm."
        else -> null
    }
    var endError = when {
        endTime.isBlank() -> "Hãy chọn giờ kết thúc."
        !isValidScheduleTime(endTime) -> "Giờ kết thúc không hợp lệ, dùng định dạng HH:mm."
        else -> null
    }

    if (startError == null && endError == null && startTime >= endTime) {
        endError = "Giờ kết thúc phải sau giờ bắt đầu."
    }

    val errors = ScheduleFormFieldErrors(
        subject = if (subjectId == null || subjectId <= 0) "Hãy chọn môn học." else null,
        studyDate = dateError,
        startTime = startError,
        endTime = endError,
        location = if (normalizedLocation.isEmpty()) "Hãy nhập địa điểm." else null,
    )

    if (errors.hasErrors) return ScheduleValidationResult(errors = errors)

    return ScheduleValidationResult(
        fields = ValidatedScheduleFields(
            subjectId = requireNotNull(subjectId),
            studyDate = studyDate,
            startTime = startTime,
            endTime = endTime,
            location = normalizedLocation,
        ),
    )
}

fun isValidScheduleDate(value: String): Boolean {
    if (!strictDatePattern.matches(value)) return false
    return try {
        LocalDate.parse(value, scheduleDateFormatter)
        true
    } catch (_: DateTimeException) {
        false
    }
}

fun isValidScheduleTime(value: String): Boolean {
    if (!strictTimePattern.matches(value)) return false
    return try {
        LocalTime.parse(value, scheduleTimeFormatter)
        true
    } catch (_: DateTimeException) {
        false
    }
}

fun formatScheduleDate(year: Int, month: Int, dayOfMonth: Int): String =
    LocalDate.of(year, month, dayOfMonth).format(scheduleDateFormatter)

fun formatScheduleTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(scheduleTimeFormatter)
