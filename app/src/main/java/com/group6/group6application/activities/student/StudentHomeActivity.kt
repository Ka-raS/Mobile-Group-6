package com.group6.group6application.activities.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeActivity(
    username: String,
    onSchedules: () -> Unit,
    onBook: () -> Unit,
    onCalendar: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag("student_home_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trang sinh viên",
                        modifier = Modifier.testTag("student_home_title"),
                    )
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = if (username.isBlank()) "Xin chào!" else "Xin chào, $username!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .testTag("student_home_welcome")
                    .semantics { contentDescription = "Lời chào sinh viên" },
            )
            Text(
                text = "Bạn muốn làm gì?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            HomeActionButton(
                label = "Lịch học của tôi",
                contentDescription = "Mở lịch học của tôi",
                testTag = "student_home_schedules_button",
                onClick = onSchedules,
            )
            HomeActionButton(
                label = "Đặt lịch học",
                contentDescription = "Đặt một buổi học",
                testTag = "student_home_book_button",
                onClick = onBook,
            )
            HomeActionButton(
                label = "Lịch",
                contentDescription = "Mở lịch",
                testTag = "student_home_calendar_button",
                onClick = onCalendar,
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_home_logout_button")
                    .semantics { contentDescription = "Đăng xuất" },
            ) {
                Text("Đăng xuất")
            }
        }
    }
}

@Composable
private fun HomeActionButton(
    label: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Text(label)
    }
}
