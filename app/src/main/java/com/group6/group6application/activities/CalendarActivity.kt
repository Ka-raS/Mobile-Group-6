package com.group6.group6application.activities

import androidx.compose.runtime.Composable
import com.group6.group6application.activities.calendar.CalendarScreen

@Composable
fun CalendarActivity(onBack: () -> Unit) {
    CalendarScreen(onBack)
}
