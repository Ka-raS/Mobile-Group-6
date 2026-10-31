package com.group6.group6application.activities

import androidx.compose.runtime.Composable
import com.group6.group6application.activities.navigation.PlaceholderScreen

@Composable
fun CalendarActivity(onBack: () -> Unit) {
    PlaceholderScreen("Calendar", "Back" to onBack)
}
