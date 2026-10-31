package com.group6.group6application.activities.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.group6.group6application.activities.theme.Group6ApplicationTheme

@Composable
fun AdminHomeScreen(onStudents: () -> Unit, onSubjects: () -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Admin Home", style = MaterialTheme.typography.headlineMedium)
        Text("Manage students and subjects for the study schedule app.", style = MaterialTheme.typography.bodyLarge)
        Card(onClick = onStudents, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Student Management", style = MaterialTheme.typography.titleLarge)
                Text("View, search, and update student profiles and create Student accounts.")
            }
        }
        Card(onClick = onSubjects, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Subject Management", style = MaterialTheme.typography.titleLarge)
                Text("Manage subject codes, names, descriptions, and credits.")
            }
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminHomePreview() {
    Group6ApplicationTheme(dynamicColor = false) { AdminHomeScreen({}, {}, {}) }
}
