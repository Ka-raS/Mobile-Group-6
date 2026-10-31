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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListActivity(
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
                title = { Text("Lịch học của tôi") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("schedule_list_back_button")
                            .semantics { contentDescription = "Quay lại" },
                    ) {
                        Text("Quay lại")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onAdd,
                        enabled = sessionValid && !isBusy,
                        modifier = Modifier
                            .testTag("schedule_list_add_button")
                            .semantics { contentDescription = "Thêm lịch học" },
                    ) {
                        Text("Thêm")
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
            title = { Text("Xóa lịch học?") },
            text = { Text("Lịch học này sẽ bị xóa vĩnh viễn.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        schedulePendingDeletion = null
                        onDelete(schedule)
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .testTag("schedule_delete_confirm_button")
                        .semantics { contentDescription = "Xác nhận xóa lịch học" },
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { schedulePendingDeletion = null },
                    modifier = Modifier
                        .testTag("schedule_delete_cancel_button")
                        .semantics { contentDescription = "Hủy xóa lịch học" },
                ) {
                    Text("Hủy")
                }
            },
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissError,
            modifier = Modifier.testTag("schedule_error_dialog"),
            title = { Text("Lỗi lịch học") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onDismissError,
                    modifier = Modifier
                        .testTag("schedule_error_dismiss_button")
                        .semantics { contentDescription = "Đóng thông báo lỗi" },
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
            label = { Text("Tìm lịch học") },
            placeholder = { Text("Môn học hoặc địa điểm") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("schedule_search_field")
                .semantics { contentDescription = "Tìm lịch học theo môn học hoặc địa điểm" },
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
                label = "Tất cả",
                status = null,
                selectedStatus = selectedStatus,
                enabled = !isBusy,
                onStatusFilterChange = onStatusFilterChange,
            )
            ScheduleStatus.ALL.forEach { status ->
                StatusFilterChip(
                    label = ScheduleStatus.label(status),
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
            .semantics { contentDescription = "Lọc lịch học: $label" },
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
                        "${schedule.startTime} đến ${schedule.endTime}, ${schedule.location}, " +
                        ScheduleStatus.label(schedule.status)
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
            ScheduleDetail(label = "Ngày", value = schedule.studyDate)
            ScheduleDetail(
                label = "Giờ",
                value = "${schedule.startTime}–${schedule.endTime}",
            )
            ScheduleDetail(label = "Địa điểm", value = schedule.location)
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
                        .semantics { contentDescription = "Sửa lịch học ${item.subjectCode}" },
                ) {
                    Text("Sửa")
                }
                TextButton(
                    onClick = onDeleteRequest,
                    enabled = !isBusy,
                    modifier = Modifier
                        .testTag("schedule_delete_${schedule.id}")
                        .semantics { contentDescription = "Xóa lịch học ${item.subjectCode}" },
                ) {
                    Text("Xóa")
                }
                Box {
                    TextButton(
                        onClick = { statusMenuExpanded = true },
                        enabled = !isBusy,
                        modifier = Modifier
                            .testTag("schedule_status_menu_${schedule.id}")
                            .semantics {
                                contentDescription = "Đổi trạng thái lịch học ${item.subjectCode}"
                            },
                    ) {
                        Text("Trạng thái")
                    }
                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false },
                        modifier = Modifier.testTag("schedule_status_options_${schedule.id}"),
                    ) {
                        ScheduleStatus.ALL.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(ScheduleStatus.label(status)) },
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
                                            "Đặt lịch học ${item.subjectCode} thành ${ScheduleStatus.label(status)}"
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
            text = ScheduleStatus.label(status),
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
                text = if (hasActiveFilters) "Không có lịch học phù hợp" else "Chưa có lịch học",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (hasActiveFilters) {
                    "Hãy thử đổi từ khóa hoặc bộ lọc trạng thái."
                } else {
                    "Thêm lịch học để bắt đầu."
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
                    text = "Không có phiên đăng nhập",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Vui lòng đăng nhập bằng tài khoản sinh viên để quản lý lịch học.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
