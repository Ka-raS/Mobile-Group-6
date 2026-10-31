package com.group6.group6application.activities.student.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.group6.group6application.entities.Subject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SCREEN_TAG = "schedule_form_screen"
private const val BACK_TAG = "schedule_form_back"
private const val SUBJECT_TAG = "schedule_form_subject"
private const val DATE_TAG = "schedule_form_date"
private const val START_TIME_TAG = "schedule_form_start_time"
private const val END_TIME_TAG = "schedule_form_end_time"
private const val LOCATION_TAG = "schedule_form_location"
private const val STATUS_TAG = "schedule_form_status"
private const val ERROR_TAG = "schedule_form_error"
private const val LOADING_TAG = "schedule_form_loading"
private const val SAVE_TAG = "schedule_form_save"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleFormActivity(
    isEditing: Boolean,
    subjects: List<Subject>,
    selectedSubjectId: Int?,
    studyDate: String,
    startTime: String,
    endTime: String,
    location: String,
    status: String?,
    isLoading: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    errorDismissible: Boolean = true,
    fieldErrors: ScheduleFormFieldErrors,
    sessionValid: Boolean,
    onSubjectChange: (Int) -> Unit,
    onDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val interactionEnabled =
        !isLoading && !isSaving && sessionValid && errorMessage.isNullOrBlank()
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag(SCREEN_TAG),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit schedule" else "Book a session") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier =
                            Modifier.testTag(BACK_TAG).semantics {
                                contentDescription = "Navigate back"
                            },
                    ) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).testTag(LOADING_TAG),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Loading schedule…")
                }
            }
        } else {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!sessionValid) {
                    MessageBanner(
                        message = "Your student session is unavailable. Please sign in again.",
                        dismissible = false,
                        onDismiss = onDismissError,
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    MessageBanner(
                        message = errorMessage,
                        dismissible = errorDismissible,
                        onDismiss = onDismissError,
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = {
                        if (interactionEnabled && subjects.isNotEmpty()) {
                            subjectMenuExpanded = !subjectMenuExpanded
                        }
                    },
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.displayName().orEmpty(),
                        onValueChange = {},
                        modifier =
                            Modifier.fillMaxWidth()
                                .menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = interactionEnabled && subjects.isNotEmpty(),
                                )
                                .testTag(SUBJECT_TAG)
                                .semantics { contentDescription = "Select subject" },
                        enabled = interactionEnabled && subjects.isNotEmpty(),
                        readOnly = true,
                        singleLine = true,
                        isError = fieldErrors.subject != null,
                        label = { Text("Subject") },
                        placeholder = { Text("Choose a subject") },
                        supportingText =
                            fieldErrors.subject?.let { message ->
                                {
                                    Text(
                                        text = message,
                                        modifier =
                                            Modifier.testTag("schedule_form_subject_error"),
                                    )
                                }
                            },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = subjectMenuExpanded
                            )
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false },
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.displayName()) },
                                onClick = {
                                    subjectMenuExpanded = false
                                    onSubjectChange(subject.id)
                                },
                                modifier =
                                    Modifier.testTag("schedule_form_subject_option_${subject.id}"),
                            )
                        }
                    }
                }

                if (subjects.isEmpty()) {
                    Text(
                        text = "No subjects are available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                PickerField(
                    value = studyDate,
                    label = "Study date",
                    placeholder = "YYYY-MM-DD",
                    trailingText = "Date",
                    testTag = DATE_TAG,
                    contentDescription = "Select study date",
                    enabled = interactionEnabled,
                    errorMessage = fieldErrors.studyDate,
                    onClick = { showDatePicker = true },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PickerField(
                        value = startTime,
                        label = "Start time",
                        placeholder = "HH:mm",
                        trailingText = "Time",
                        testTag = START_TIME_TAG,
                        contentDescription = "Select start time",
                        enabled = interactionEnabled,
                        errorMessage = fieldErrors.startTime,
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f),
                    )
                    PickerField(
                        value = endTime,
                        label = "End time",
                        placeholder = "HH:mm",
                        trailingText = "Time",
                        testTag = END_TIME_TAG,
                        contentDescription = "Select end time",
                        enabled = interactionEnabled,
                        errorMessage = fieldErrors.endTime,
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f),
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(LOCATION_TAG)
                            .semantics { contentDescription = "Schedule location" },
                    enabled = interactionEnabled,
                    singleLine = true,
                    isError = fieldErrors.location != null,
                    label = { Text("Location") },
                    placeholder = { Text("Enter a location") },
                    supportingText =
                        fieldErrors.location?.let { message ->
                            {
                                Text(
                                    text = message,
                                    modifier = Modifier.testTag("schedule_form_location_error"),
                                )
                            }
                        },
                )

                if (isEditing && !status.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag(STATUS_TAG),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Current status",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag(SAVE_TAG),
                    enabled = interactionEnabled,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.size(10.dp))
                        Text("Saving…")
                    } else {
                        Text(if (isEditing) "Save changes" else "Save schedule")
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = studyDate.toUtcDateMillis())

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onDateChange(selectedDate.toUtcDateString())
                            showDatePicker = false
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                    modifier = Modifier.testTag("schedule_form_date_confirm"),
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.testTag("schedule_form_date_cancel"),
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.testTag("schedule_form_date_picker"),
            )
        }
    }

    if (showStartTimePicker) {
        ScheduleTimePickerDialog(
            title = "Select start time",
            initialTime = startTime,
            testTagPrefix = "schedule_form_start_time_picker",
            onConfirm = {
                onStartTimeChange(it)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
        )
    }

    if (showEndTimePicker) {
        ScheduleTimePickerDialog(
            title = "Select end time",
            initialTime = endTime,
            testTagPrefix = "schedule_form_end_time_picker",
            onConfirm = {
                onEndTimeChange(it)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
        )
    }
}

@Composable
private fun PickerField(
    value: String,
    label: String,
    placeholder: String,
    trailingText: String,
    testTag: String,
    contentDescription: String,
    enabled: Boolean,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            singleLine = true,
            isError = errorMessage != null,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { Text(trailingText, style = MaterialTheme.typography.labelSmall) },
            supportingText =
                errorMessage?.let { message ->
                    {
                        Text(
                            text = message,
                            modifier = Modifier.testTag("${testTag}_error"),
                        )
                    }
                },
        )
        Box(
            modifier =
                Modifier.matchParentSize()
                    .testTag(testTag)
                    .semantics { this.contentDescription = contentDescription }
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        )
    }
}

@Composable
private fun MessageBanner(message: String, dismissible: Boolean, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag(ERROR_TAG),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (dismissible) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("schedule_form_error_dismiss"),
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTimePickerDialog(
    title: String,
    initialTime: String,
    testTagPrefix: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val parsedTime = remember(initialTime) { initialTime.toLocalTimeOrNow() }
    val timePickerState =
        rememberTimePickerState(
            initialHour = parsedTime.hour,
            initialMinute = parsedTime.minute,
            is24Hour = true,
        )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(formatTime(timePickerState.hour, timePickerState.minute))
                },
                modifier = Modifier.testTag("${testTagPrefix}_confirm"),
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("${testTagPrefix}_cancel"),
            ) {
                Text("Cancel")
            }
        },
    ) {
        TimePicker(
            state = timePickerState,
            modifier = Modifier.testTag(testTagPrefix),
        )
    }
}

private fun Subject.displayName(): String = "$subjectCode — $subjectName"

private fun String.toUtcDateMillis(): Long? =
    runCatching {
            LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
        .getOrNull()

private fun Long.toUtcDateString(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun String.toLocalTimeOrNow(): LocalTime =
    runCatching { LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm")) }
        .getOrElse { LocalTime.now() }

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", hour, minute)
