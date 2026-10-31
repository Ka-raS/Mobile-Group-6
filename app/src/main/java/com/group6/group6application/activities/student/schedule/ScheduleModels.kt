package com.group6.group6application.activities.student.schedule

import com.group6.group6application.entities.ScheduleWithSubject
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.Subject

data class ScheduleListUiState(
    val schedules: List<ScheduleWithSubject> = emptyList(),
    val query: String = "",
    val statusFilter: String? = null,
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val sessionValid: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface ScheduleListEvent {
    data class ShowMessage(val message: String) : ScheduleListEvent
}

data class ScheduleFormUiState(
    val scheduleId: Int = 0,
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: Int? = null,
    val studyDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val status: String = ScheduleStatus.PENDING,
    val fieldErrors: ScheduleFormFieldErrors = ScheduleFormFieldErrors(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val sessionValid: Boolean = true,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean get() = scheduleId > 0
    val canSave: Boolean get() = !isLoading && !isSaving && errorMessage == null
}

sealed interface ScheduleFormEvent {
    data class Saved(val scheduleId: Int) : ScheduleFormEvent
    data class ShowMessage(val message: String) : ScheduleFormEvent
}
