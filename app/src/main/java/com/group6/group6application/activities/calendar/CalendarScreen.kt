package com.group6.group6application.activities.calendar

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.dal.StudyScheduleDao
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.ScheduleWithSubject
import kotlinx.coroutines.flow.catch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val calendarLocale = Locale.forLanguageTag("vi-VN")
private val fullDateFormat = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", calendarLocale)
private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM", calendarLocale)
private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy", calendarLocale)
private val weekdayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

private sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Ready(val schedules: List<ScheduleWithSubject>) : CalendarUiState
    data object Error : CalendarUiState
}

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember(context) { SessionManager(context) }
    if (!session.isLoggedIn || session.role != Role.STUDENT || session.studentId <= 0) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Không tìm thấy hồ sơ sinh viên. Vui lòng đăng nhập lại.")
            TextButton(onClick = onBack) { Text("Quay lại") }
        }
        return
    }
    val db = remember(context) { AppDB.get(context) }
    StudentCalendar(session.studentId, db.studyScheduleDao(), onBack)
}

@Composable
internal fun StudentCalendar(studentId: Int, scheduleDao: StudyScheduleDao, onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var modeName by rememberSaveable { mutableStateOf(CalendarMode.MONTH.name) }
    var retry by remember { mutableIntStateOf(0) }
    val date = LocalDate.ofEpochDay(selectedDay)
    val mode = CalendarMode.valueOf(modeName)
    val range = remember(date, mode) { calendarRange(date, mode) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by produceState<CalendarUiState>(
        CalendarUiState.Loading, studentId, scheduleDao, range, lifecycle, retry,
    ) {
        value = CalendarUiState.Loading
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            scheduleDao.getInRange(studentId, range.start.toString(), range.end.toString())
                .catch { value = CalendarUiState.Error }
                .collect { value = CalendarUiState.Ready(it) }
        }
    }
    val schedules = (state as? CalendarUiState.Ready)?.schedules.orEmpty()
    val byDate = remember(schedules) { schedules.groupBy { it.schedule.studyDate } }
    val today = LocalDate.now()

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("calendar-list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Quay lại") }
                Text("Lịch học", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { selectedDay = today.toEpochDay() }) { Text("Hôm nay") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalendarMode.entries.forEach { option ->
                    FilterChip(
                        selected = mode == option,
                        onClick = { modeName = option.name },
                        label = { Text(option.label, Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            PeriodHeader(
                date, mode,
                onPrevious = { selectedDay = moveCalendarDate(date, mode, -1).toEpochDay() },
                onNext = { selectedDay = moveCalendarDate(date, mode, 1).toEpochDay() },
            )
            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> selectedDay = LocalDate.of(year, month + 1, day).toEpochDay() },
                        date.year, date.monthValue - 1, date.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Chọn ngày: ${date.format(shortDateFormat)}/${date.year}") }
        }
        if (mode == CalendarMode.MONTH) {
            item { MonthGrid(date, today, byDate) { selectedDay = it.toEpochDay() } }
        } else if (mode == CalendarMode.WEEK) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { index ->
                        val day = range.start.plusDays(index.toLong())
                        DateCell(
                            day, day == date, day == today, true, byDate[day.toString()].orEmpty().size,
                            onClick = { selectedDay = day.toEpochDay(); modeName = CalendarMode.DAY.name },
                            modifier = Modifier.weight(1f), weekday = weekdayLabels[index],
                        )
                    }
                }
            }
        }
        when (state) {
            CalendarUiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.semantics { contentDescription = "Đang tải lịch" })
                }
            }
            CalendarUiState.Error -> item {
                CalendarMessage("Không thể tải lịch học. Vui lòng thử lại.")
                TextButton(onClick = { retry++ }) { Text("Thử lại") }
            }
            is CalendarUiState.Ready -> if (mode == CalendarMode.WEEK) {
                item { Text("Lịch trong tuần · ${schedules.size} buổi", style = MaterialTheme.typography.titleMedium) }
                repeat(7) { index ->
                    val day = range.start.plusDays(index.toLong())
                    dayAgenda(day, byDate[day.toString()].orEmpty(), today)
                }
            } else {
                dayAgenda(date, byDate[date.toString()].orEmpty(), today)
            }
        }
    }
}

@Composable
private fun PeriodHeader(date: LocalDate, mode: CalendarMode, onPrevious: () -> Unit, onNext: () -> Unit) {
    val title = when (mode) {
        CalendarMode.DAY -> date.format(fullDateFormat)
        CalendarMode.WEEK -> weekStart(date).let {
            "${it.format(shortDateFormat)} – ${it.plusDays(6).format(shortDateFormat)}/${it.plusDays(6).year}"
        }
        CalendarMode.MONTH -> date.format(monthFormat)
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onPrevious, Modifier.semantics { contentDescription = "${mode.label} trước" }) {
            Text("‹", style = MaterialTheme.typography.headlineSmall)
        }
        Text(title, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        TextButton(onNext, Modifier.semantics { contentDescription = "${mode.label} sau" }) {
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun MonthGrid(
    selectedDate: LocalDate,
    today: LocalDate,
    byDate: Map<String, List<ScheduleWithSubject>>,
    onSelect: (LocalDate) -> Unit,
) {
    val month = YearMonth.from(selectedDate)
    val days = remember(month) { monthDays(selectedDate) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.forEach {
                Text(
                    it, Modifier.weight(1f).padding(vertical = 8.dp), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    DateCell(
                        day, day == selectedDate, day == today, YearMonth.from(day) == month,
                        byDate[day.toString()].orEmpty().size, onClick = { onSelect(day) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Text(
            "Số dưới ngày là tổng số buổi học, bao gồm lịch đã hủy.", Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    selected: Boolean,
    today: Boolean,
    inMonth: Boolean,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    weekday: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp).semantics {
            contentDescription = "${date.format(fullDateFormat)}, $count buổi học" +
                (if (today) ", hôm nay" else "") + (if (selected) ", đang chọn" else "")
        },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        contentColor = when {
            selected -> colors.onPrimaryContainer
            !inMonth -> colors.onSurfaceVariant.copy(alpha = 0.55f)
            else -> colors.onSurface
        },
        border = if (today) BorderStroke(1.dp, colors.primary) else null,
    ) {
        Column(
            Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            weekday?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            Text(
                date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || today) FontWeight.Bold else FontWeight.Normal,
            )
            if (count > 0) Text(count.toString(), style = MaterialTheme.typography.labelSmall)
            else Spacer(Modifier.height(16.dp))
        }
    }
}

private fun LazyListScope.dayAgenda(date: LocalDate, schedules: List<ScheduleWithSubject>, today: LocalDate) {
    item(key = "heading-$date") {
        Column {
            Text(date.format(fullDateFormat) + if (date == today) " · Hôm nay" else "", style = MaterialTheme.typography.titleMedium)
            Text("${schedules.size} buổi học", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (schedules.isEmpty()) {
        item(key = "empty-$date") { CalendarMessage("Không có lịch học trong ngày này.") }
    } else {
        items(schedules, key = { "schedule-${it.schedule.id}" }) { ScheduleCard(it) }
    }
}

@Composable
private fun CalendarMessage(message: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Text(message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScheduleCard(item: ScheduleWithSubject) {
    val schedule = item.schedule
    val colors = MaterialTheme.colorScheme
    val (label, color) = when (schedule.status) {
        ScheduleStatus.PENDING -> "Chờ xác nhận" to colors.tertiary
        ScheduleStatus.CONFIRMED -> "Đã xác nhận" to colors.primary
        ScheduleStatus.CANCELLED -> "Đã hủy" to colors.error
        else -> schedule.status to colors.onSurfaceVariant
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${schedule.startTime} – ${schedule.endTime}", style = MaterialTheme.typography.titleMedium, color = colors.primary)
            Text(item.subjectName, style = MaterialTheme.typography.titleMedium)
            Text(item.subjectCode, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Text("Địa điểm: ${schedule.location}", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(Modifier.size(8.dp), shape = CircleShape, color = color) {}
                Text(label, color = color, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
