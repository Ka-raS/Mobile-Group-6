package com.group6.group6application.activities

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.group6.group6application.activities.navigation.Routes
import com.group6.group6application.activities.student.StudentHomeScreen
import com.group6.group6application.activities.student.schedule.ScheduleFormEvent
import com.group6.group6application.activities.student.schedule.ScheduleFormScreen
import com.group6.group6application.activities.student.schedule.ScheduleFormViewModel
import com.group6.group6application.activities.student.schedule.ScheduleListEvent
import com.group6.group6application.activities.student.schedule.ScheduleListScreen
import com.group6.group6application.activities.student.schedule.ScheduleListViewModel
import com.group6.group6application.dal.SessionManager

fun NavGraphBuilder.studentGraph(navController: NavHostController, onLogout: () -> Unit) {
    composable(Routes.STUDENT_HOME) {
        val context = LocalContext.current
        val session = remember(context) { SessionManager(context) }
        StudentHomeScreen(
            username = session.username,
            onSchedules = { navController.navigate(Routes.STUDENT_SCHEDULES) },
            onBook = { navController.navigate(Routes.studentScheduleForm()) },
            onCalendar = { navController.navigate(Routes.STUDENT_CALENDAR) },
            onLogout = onLogout,
        )
    }
    composable(Routes.STUDENT_SCHEDULES) {
        val scheduleViewModel: ScheduleListViewModel = viewModel()
        val state by scheduleViewModel.uiState.collectAsState()
        var transientMessage by remember { mutableStateOf<String?>(null) }
        var stateErrorDismissed by remember(state.errorMessage) { mutableStateOf(false) }

        LaunchedEffect(scheduleViewModel) {
            scheduleViewModel.events.collect { event ->
                if (event is ScheduleListEvent.ShowMessage) {
                    transientMessage = event.message
                }
            }
        }

        val stateError = state.errorMessage?.takeUnless {
            stateErrorDismissed || !state.sessionValid
        }
        ScheduleListScreen(
            schedules = state.schedules,
            query = state.query,
            selectedStatus = state.statusFilter,
            isBusy = state.isLoading || state.isMutating,
            errorMessage = transientMessage ?: stateError,
            sessionValid = state.sessionValid,
            onQueryChange = scheduleViewModel::onQueryChange,
            onStatusFilterChange = scheduleViewModel::onStatusFilterChange,
            onAdd = { navController.navigate(Routes.studentScheduleForm()) },
            onEdit = { id -> navController.navigate(Routes.studentScheduleForm(id)) },
            onDelete = { schedule -> scheduleViewModel.deleteSchedule(schedule.id) },
            onStatusChange = { schedule, status ->
                scheduleViewModel.changeStatus(schedule.id, status)
            },
            onDismissError = {
                if (transientMessage != null) {
                    transientMessage = null
                } else {
                    stateErrorDismissed = true
                }
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable(Routes.STUDENT_SCHEDULE_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val scheduleViewModel: ScheduleFormViewModel = viewModel()
        val state by scheduleViewModel.uiState.collectAsState()
        var transientMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(scheduleViewModel) {
            scheduleViewModel.events.collect { event ->
                when (event) {
                    is ScheduleFormEvent.Saved -> navController.popBackStack()
                    is ScheduleFormEvent.ShowMessage -> transientMessage = event.message
                }
            }
        }

        val visibleMessage = transientMessage ?: state.errorMessage
        ScheduleFormScreen(
            isEditing = state.isEditing,
            subjects = state.subjects,
            selectedSubjectId = state.selectedSubjectId,
            studyDate = state.studyDate,
            startTime = state.startTime,
            endTime = state.endTime,
            location = state.location,
            status = state.status,
            isLoading = state.isLoading,
            isSaving = state.isSaving,
            errorMessage = visibleMessage,
            errorDismissible = transientMessage != null,
            fieldErrors = state.fieldErrors,
            sessionValid = state.sessionValid,
            onSubjectChange = scheduleViewModel::onSubjectSelected,
            onDateChange = scheduleViewModel::onDateChange,
            onStartTimeChange = scheduleViewModel::onStartTimeChange,
            onEndTimeChange = scheduleViewModel::onEndTimeChange,
            onLocationChange = scheduleViewModel::onLocationChange,
            onSave = scheduleViewModel::save,
            onDismissError = { transientMessage = null },
            onBack = { navController.popBackStack() },
        )
    }
    composable(Routes.STUDENT_CALENDAR) {
        CalendarActivity(onBack = { navController.popBackStack() })
    }
}
