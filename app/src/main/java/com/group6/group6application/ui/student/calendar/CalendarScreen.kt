package com.group6.group6application.ui.student.calendar

import androidx.compose.runtime.Composable
import com.group6.group6application.ui.navigation.PlaceholderScreen

// Người 4: daily / weekly / monthly view. Data: studyScheduleDao().getInRange(studentId, from, to)
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    PlaceholderScreen("Calendar", "Back" to onBack)
}
