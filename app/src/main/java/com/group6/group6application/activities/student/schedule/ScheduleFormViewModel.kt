package com.group6.group6application.activities.student.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScheduleFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val database = AppDB.get(application)
    private val scheduleDao = database.studyScheduleDao()
    private val subjectDao = database.subjectDao()
    private val sessionManager = SessionManager(application)
    private val requestedScheduleId = savedStateHandle.get<Int>("id") ?: 0

    private val _uiState = MutableStateFlow(
        ScheduleFormUiState(
            scheduleId = requestedScheduleId,
            sessionValid = sessionManager.studentId > 0,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScheduleFormEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        loadForm()
    }

    fun onSubjectSelected(subjectId: Int) {
        val errors = _uiState.value.fieldErrors.copy(subject = null)
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId, fieldErrors = errors)
    }

    fun onDateChange(value: String) {
        val errors = _uiState.value.fieldErrors.copy(studyDate = null)
        _uiState.value = _uiState.value.copy(studyDate = value, fieldErrors = errors)
    }

    fun onStartTimeChange(value: String) {
        val errors = _uiState.value.fieldErrors.copy(startTime = null, endTime = null)
        _uiState.value = _uiState.value.copy(startTime = value, fieldErrors = errors)
    }

    fun onEndTimeChange(value: String) {
        val errors = _uiState.value.fieldErrors.copy(endTime = null)
        _uiState.value = _uiState.value.copy(endTime = value, fieldErrors = errors)
    }

    fun onLocationChange(value: String) {
        val errors = _uiState.value.fieldErrors.copy(location = null)
        _uiState.value = _uiState.value.copy(location = value, fieldErrors = errors)
    }

    fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.errorMessage != null) return

        val validation = validateScheduleForm(
            subjectId = state.selectedSubjectId,
            studyDate = state.studyDate,
            startTime = state.startTime,
            endTime = state.endTime,
            location = state.location,
        )
        if (!validation.isValid) {
            _uiState.value = state.copy(fieldErrors = validation.errors)
            _events.tryEmit(ScheduleFormEvent.ShowMessage("Vui lòng sửa các trường bị đánh dấu."))
            return
        }

        _uiState.value = state.copy(isSaving = true, fieldErrors = ScheduleFormFieldErrors())
        viewModelScope.launch {
            try {
                saveValidated(requireNotNull(validation.fields))
            } catch (_: Exception) {
                _events.emit(ScheduleFormEvent.ShowMessage("Không thể lưu lịch học."))
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    private fun loadForm() {
        viewModelScope.launch {
            val studentId = sessionManager.studentId
            if (studentId <= 0) {
                blockForm(NO_STUDENT_SESSION, sessionValid = false)
                return@launch
            }
            if (requestedScheduleId < 0) {
                blockForm("Không tìm thấy lịch học.")
                return@launch
            }

            try {
                val subjects = subjectDao.getAll().first()
                if (requestedScheduleId == 0) {
                    _uiState.value = _uiState.value.copy(
                        subjects = subjects,
                        status = ScheduleStatus.PENDING,
                        isLoading = false,
                        sessionValid = true,
                    )
                    return@launch
                }

                val schedule = scheduleDao.getById(requestedScheduleId)
                if (schedule == null) {
                    blockForm("Không tìm thấy lịch học.", subjects = subjects)
                    return@launch
                }
                if (schedule.studentId != studentId) {
                    blockForm("Bạn chỉ được sửa lịch học của mình.", subjects = subjects)
                    return@launch
                }

                _uiState.value = ScheduleFormUiState(
                    scheduleId = schedule.id,
                    subjects = subjects,
                    selectedSubjectId = schedule.subjectId,
                    studyDate = schedule.studyDate,
                    startTime = schedule.startTime,
                    endTime = schedule.endTime,
                    location = schedule.location,
                    status = schedule.status,
                    isLoading = false,
                    sessionValid = true,
                )
            } catch (_: Exception) {
                blockForm("Không thể tải form lịch học.")
            }
        }
    }

    private suspend fun saveValidated(fields: ValidatedScheduleFields) {
        val studentId = sessionManager.studentId
        if (studentId <= 0) {
            blockForm(NO_STUDENT_SESSION, sessionValid = false)
            return
        }

        if (subjectDao.getById(fields.subjectId) == null) {
            val errors = _uiState.value.fieldErrors.copy(subject = "Hãy chọn một môn học còn tồn tại.")
            _uiState.value = _uiState.value.copy(fieldErrors = errors)
            _events.emit(ScheduleFormEvent.ShowMessage("Môn học đã chọn không còn tồn tại."))
            return
        }

        val existing = if (requestedScheduleId == 0) {
            null
        } else {
            scheduleDao.getById(requestedScheduleId).also { schedule ->
                if (schedule == null) {
                    blockForm("Không tìm thấy lịch học.")
                    return
                }
                if (schedule.studentId != studentId) {
                    blockForm("Bạn chỉ được sửa lịch học của mình.")
                    return
                }
            }
        }

        val status = existing?.status ?: ScheduleStatus.PENDING
        val schedule = StudySchedule(
            id = existing?.id ?: 0,
            studentId = studentId,
            subjectId = fields.subjectId,
            studyDate = fields.studyDate,
            startTime = fields.startTime,
            endTime = fields.endTime,
            location = fields.location,
            status = status,
        )

        if (status != ScheduleStatus.CANCELLED && schedule.overlapsAnotherSchedule()) {
            _events.emit(ScheduleFormEvent.ShowMessage(OVERLAP_MESSAGE))
            return
        }

        val savedSchedule = if (existing == null) {
            val insertedId = scheduleDao.insert(schedule)
            if (insertedId <= 0 || insertedId > Int.MAX_VALUE) {
                throw IllegalStateException("Room trả về id lịch học không hợp lệ")
            }
            schedule.copy(id = insertedId.toInt())
        } else {
            scheduleDao.update(schedule)
            schedule
        }

        if (existing == null) {
            NotificationScheduler.schedule(getApplication(), savedSchedule)
        } else {
            NotificationScheduler.notifyChanged(getApplication(), savedSchedule)
        }

        _uiState.value = _uiState.value.copy(
            scheduleId = savedSchedule.id,
            selectedSubjectId = savedSchedule.subjectId,
            studyDate = savedSchedule.studyDate,
            startTime = savedSchedule.startTime,
            endTime = savedSchedule.endTime,
            location = savedSchedule.location,
            status = savedSchedule.status,
        )
        _events.emit(ScheduleFormEvent.Saved(savedSchedule.id))
    }

    private suspend fun StudySchedule.overlapsAnotherSchedule(): Boolean =
        scheduleDao.countOverlap(
            studentId = studentId,
            date = studyDate,
            start = startTime,
            end = endTime,
            excludeId = id,
        ) > 0

    private fun blockForm(
        message: String,
        sessionValid: Boolean = true,
        subjects: List<com.group6.group6application.entities.Subject> = _uiState.value.subjects,
    ) {
        _uiState.value = _uiState.value.copy(
            subjects = subjects,
            isLoading = false,
            isSaving = false,
            sessionValid = sessionValid,
            errorMessage = message,
        )
    }

    private companion object {
        const val NO_STUDENT_SESSION = "Không có phiên đăng nhập sinh viên."
        const val OVERLAP_MESSAGE = "Lịch học này bị trùng giờ với một lịch học khác."
    }
}
