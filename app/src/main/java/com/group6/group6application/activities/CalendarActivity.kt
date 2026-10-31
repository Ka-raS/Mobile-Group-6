package com.group6.group6application.activities

import androidx.compose.runtime.Composable
import com.group6.group6application.activities.calendar.CalendarViewActivity

@Composable
fun CalendarActivity(onBack: () -> Unit) {
    CalendarViewActivity(onBack)
}
