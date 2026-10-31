package com.group6.group6application.activities.student.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.notification.NotificationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleListViewModel(application: Application) : AndroidViewModel(application) {
    private val scheduleDao = AppDB.get(application).studyScheduleDao()
    private val sessionManager = SessionManager(application)

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<String?>(null)
    private val isMutating = MutableStateFlow(false)

    private val _events = MutableSharedFlow<ScheduleListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private data class SearchParameters(val query: String, val status: String?)

    private val searchState = combine(query, statusFilter, ::SearchParameters)
        .flatMapLatest { parameters ->
            val studentId = sessionManager.studentId
            if (studentId <= 0) {
                flowOf(
                    ScheduleListUiState(
                        query = parameters.query,
                        statusFilter = parameters.status,
                        isLoading = false,
                        sessionValid = false,
                        errorMessage = NO_STUDENT_SESSION,
                    ),
                )
            } else {
                scheduleDao.search(studentId, parameters.query.trim(), parameters.status)
                    .map { schedules ->
                        ScheduleListUiState(
                            schedules = schedules,
                            query = parameters.query,
                            statusFilter = parameters.status,
                            isLoading = false,
                            sessionValid = true,
                        )
                    }
                    .onStart {
                        emit(
                            ScheduleListUiState(
                                query = parameters.query,
                                statusFilter = parameters.status,
                                isLoading = true,
                                sessionValid = true,
                            ),
                        )
                    }
                    .catch {
                        emit(
                            ScheduleListUiState(
                                query = parameters.query,
                                statusFilter = parameters.status,
                                isLoading = false,
                                sessionValid = true,
                                errorMessage = "Không thể tải danh sách lịch học.",
                            ),
                        )
                    }
            }
        }

    val uiState: StateFlow<ScheduleListUiState> = combine(searchState, isMutating) { state, mutating ->
        state.copy(isMutating = mutating)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScheduleListUiState(sessionValid = sessionManager.studentId > 0),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusFilterChange(value: String?) {
        val normalized = value?.takeUnless { it.equals("All", ignoreCase = true) }
        if (normalized != null && normalized !in ScheduleStatus.ALL) {
            _events.tryEmit(ScheduleListEvent.ShowMessage("Trạng thái lịch học không hợp lệ."))
            return
        }
        statusFilter.value = normalized
    }

    fun deleteSchedule(scheduleId: Int) {
        runMutation("Không thể xóa lịch học.") {
            val schedule = findOwnedSchedule(scheduleId) ?: return@runMutation
            scheduleDao.delete(schedule)
            NotificationScheduler.notifyDeleted(getApplication(), schedule)
            _events.emit(ScheduleListEvent.ShowMessage("Đã xóa lịch học."))
        }
    }

    fun changeStatus(scheduleId: Int, newStatus: String) {
        if (newStatus !in ScheduleStatus.ALL) {
            _events.tryEmit(ScheduleListEvent.ShowMessage("Trạng thái lịch học không hợp lệ."))
            return
        }

        runMutation("Không thể đổi trạng thái lịch học.") {
            val schedule = findOwnedSchedule(scheduleId) ?: return@runMutation
            if (schedule.status == newStatus) return@runMutation

            if (newStatus != ScheduleStatus.CANCELLED && schedule.overlapsAnotherSchedule()) {
                _events.emit(ScheduleListEvent.ShowMessage(OVERLAP_MESSAGE))
                return@runMutation
            }

            scheduleDao.updateStatus(schedule.id, newStatus)
            NotificationScheduler.notifyChanged(getApplication(), schedule.copy(status = newStatus))
            _events.emit(ScheduleListEvent.ShowMessage("Đã đổi trạng thái thành ${ScheduleStatus.label(newStatus)}."))
        }
    }

    private fun runMutation(failureMessage: String, action: suspend () -> Unit) {
        if (isMutating.value) return
        isMutating.value = true
        viewModelScope.launch {
            try {
                action()
            } catch (_: Exception) {
                _events.emit(ScheduleListEvent.ShowMessage(failureMessage))
            } finally {
                isMutating.value = false
            }
        }
    }

    private suspend fun findOwnedSchedule(scheduleId: Int): StudySchedule? {
        val studentId = sessionManager.studentId
        if (studentId <= 0) {
            _events.emit(ScheduleListEvent.ShowMessage(NO_STUDENT_SESSION))
            return null
        }
        if (scheduleId <= 0) {
            _events.emit(ScheduleListEvent.ShowMessage("Không tìm thấy lịch học."))
            return null
        }

        val schedule = scheduleDao.getById(scheduleId)
        if (schedule == null) {
            _events.emit(ScheduleListEvent.ShowMessage("Không tìm thấy lịch học."))
            return null
        }
        if (schedule.studentId != studentId) {
            _events.emit(ScheduleListEvent.ShowMessage("Bạn chỉ được quản lý lịch học của mình."))
            return null
        }
        return schedule
    }

    private suspend fun StudySchedule.overlapsAnotherSchedule(): Boolean =
        scheduleDao.countOverlap(
            studentId = studentId,
            date = studyDate,
            start = startTime,
            end = endTime,
            excludeId = id,
        ) > 0

    private companion object {
        const val NO_STUDENT_SESSION = "Không có phiên đăng nhập sinh viên."
        const val OVERLAP_MESSAGE = "Lịch học này bị trùng giờ với một lịch học khác."
    }
}
