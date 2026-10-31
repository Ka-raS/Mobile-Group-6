package com.group6.group6application.activities.student.schedule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.ScheduleWithSubject
import com.group6.group6application.entities.StudySchedule

/**
 * Stateless schedule list UI. Filtering, searching, persistence, and authorization are owned by
 * the caller; this composable only renders the supplied state and emits user actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    schedules: List<ScheduleWithSubject>,
    query: String,
    selectedStatus: String?,
    isBusy: Boolean,
    errorMessage: String?,
    sessionValid: Boolean,
    onQueryChange: (String) -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (StudySchedule) -> Unit,
    onStatusChange: (StudySchedule, String) -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    var schedulePendingDeletion by remember { mutableStateOf<StudySchedule?>(null) }

    Scaffold(
        modifier = Modifier.testTag("schedule_list_screen"),
        topBar = {
            TopAppBar(
                title = { Text("My Schedules") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("schedule_list_back_button")
                            .semantics { contentDescription = "Go back" },
                    ) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onAdd,
                        enabled = sessionValid && !isBusy,
                        modifier = Modifier
                            .testTag("schedule_list_add_button")
                            .semantics { contentDescription = "Add schedule" },
                    ) {
                        Text("Add")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            if (isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_list_progress"),
                )
            }

            if (!sessionValid) {
                InvalidSessionMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                )
            } else {
                ScheduleListContent(
                    schedules = schedules,
                    query = query,
                    selectedStatus = selectedStatus,
                    isBusy = isBusy,
                    onQueryChange = onQueryChange,
                    onStatusFilterChange = onStatusFilterChange,
                    onEdit = onEdit,
                    onDeleteRequest = { schedulePendingDeletion = it },
                    onStatusChange = onStatusChange,
                )
            }
        }
    }

    schedulePendingDeletion?.let { schedule ->
        AlertDialog(
            onDismissRequest = { schedulePendingDeletion = null },
            modifier = Modifier.testTag("schedule_delete_confirmation_dialog"),
            title = { Text("Delete schedule?") },
            text = { Text("This schedule will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        schedulePendingDeletion = null
                        onDelete(schedule)
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .testTag("schedule_delete_confirm_button")
                        .semantics { contentDescription = "Confirm schedule deletion" },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { schedulePendingDeletion = null },
                    modifier = Modifier
                        .testTag("schedule_delete_cancel_button")
                        .semantics { contentDescription = "Cancel schedule deletion" },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissError,
            modifier = Modifier.testTag("schedule_error_dialog"),
            title = { Text("Schedule error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onDismissError,
                    modifier = Modifier
                        .testTag("schedule_error_dismiss_button")
                        .semantics { contentDescription = "Dismiss schedule error" },
                ) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun ScheduleListContent(
    schedules: List<ScheduleWithSubject>,
    query: String,
    selectedStatus: String?,
    isBusy: Boolean,
    onQueryChange: (String) -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onEdit: (Int) -> Unit,
    onDeleteRequest: (StudySchedule) -> Unit,
    onStatusChange: (StudySchedule, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            enabled = !isBusy,
            label = { Text("Search schedules") },
            placeholder = { Text("Subject or location") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("schedule_search_field")
                .semantics { contentDescription = "Search schedules by subject or location" },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("schedule_status_filters"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusFilterChip(
                label = "All",
                status = null,
                selectedStatus = selectedStatus,
                enabled = !isBusy,
                onStatusFilterChange = onStatusFilterChange,
            )
            ScheduleStatus.ALL.forEach { status ->
                StatusFilterChip(
                    label = status,
                    status = status,
                    selectedStatus = selectedStatus,
                    enabled = !isBusy,
                    onStatusFilterChange = onStatusFilterChange,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (schedules.isEmpty()) {
            EmptyScheduleMessage(
                hasActiveFilters = query.isNotBlank() || selectedStatus != null,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("schedule_empty_state"),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("schedule_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = schedules) { item ->
                    ScheduleCard(
                        item = item,
                        isBusy = isBusy,
                        onEdit = { onEdit(item.schedule.id) },
                        onDeleteRequest = { onDeleteRequest(item.schedule) },
                        onStatusChange = { status -> onStatusChange(item.schedule, status) },
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    status: String?,
    selectedStatus: String?,
    enabled: Boolean,
    onStatusFilterChange: (String?) -> Unit,
) {
    val tagSuffix = status?.lowercase() ?: "all"
    FilterChip(
        selected = selectedStatus == status,
        onClick = { onStatusFilterChange(status) },
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier
            .testTag("schedule_filter_$tagSuffix")
            .semantics { contentDescription = "Filter schedules by $label" },
    )
}

@Composable
private fun ScheduleCard(
    item: ScheduleWithSubject,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    val schedule = item.schedule
    var statusMenuExpanded by remember(schedule.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}")
            .semantics {
                contentDescription =
                    "${item.subjectCode}, ${item.subjectName}, ${schedule.studyDate}, " +
                        "${schedule.startTime} to ${schedule.endTime}, ${schedule.location}, " +
                        schedule.status
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${item.subjectCode} — ${item.subjectName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ScheduleDetail(label = "Date", value = schedule.studyDate)
            ScheduleDetail(
                label = "Time",
                value = "${schedule.startTime}–${schedule.endTime}",
            )
            ScheduleDetail(label = "Location", value = schedule.location)
            StatusBadge(status = schedule.status)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onEdit,
                    enabled = !isBusy,
                    modifier = Modifier
                        .testTag("schedule_edit_${schedule.id}")
                        .semantics { contentDescription = "Edit ${item.subjectCode} schedule" },
                ) {
                    Text("Edit")
                }
                TextButton(
                    onClick = onDeleteRequest,
                    enabled = !isBusy,
                    modifier = Modifier
                        .testTag("schedule_delete_${schedule.id}")
                        .semantics { contentDescription = "Delete ${item.subjectCode} schedule" },
                ) {
                    Text("Delete")
                }
                Box {
                    TextButton(
                        onClick = { statusMenuExpanded = true },
                        enabled = !isBusy,
                        modifier = Modifier
                            .testTag("schedule_status_menu_${schedule.id}")
                            .semantics {
                                contentDescription = "Change ${item.subjectCode} schedule status"
                            },
                    ) {
                        Text("Status")
                    }
                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false },
                        modifier = Modifier.testTag("schedule_status_options_${schedule.id}"),
                    ) {
                        ScheduleStatus.ALL.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                enabled = status != schedule.status && !isBusy,
                                onClick = {
                                    statusMenuExpanded = false
                                    onStatusChange(status)
                                },
                                modifier = Modifier
                                    .testTag(
                                        "schedule_status_${schedule.id}_${status.lowercase()}"
                                    )
                                    .semantics {
                                        contentDescription =
                                            "Set ${item.subjectCode} schedule to $status"
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDetail(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val containerColor = when (status) {
        ScheduleStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
        ScheduleStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (status) {
        ScheduleStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
        ScheduleStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.testTag("schedule_status_badge_${status.lowercase()}"),
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun EmptyScheduleMessage(
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (hasActiveFilters) "No matching schedules" else "No schedules yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (hasActiveFilters) {
                    "Try changing your search or status filter."
                } else {
                    "Add a schedule to get started."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InvalidSessionMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.testTag("schedule_invalid_session_message"),
        contentAlignment = Alignment.Center,
    ) {
        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Session unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Please log in as a student to manage schedules.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
