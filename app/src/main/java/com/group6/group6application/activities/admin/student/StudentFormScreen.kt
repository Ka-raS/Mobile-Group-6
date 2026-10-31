package com.group6.group6application.activities.admin.student

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group6.group6application.activities.admin.*
import com.group6.group6application.activities.theme.Group6ApplicationTheme

@Composable
fun StudentFormScreen(id: Int, onBack: () -> Unit, model: AdminViewModel = viewModel()) {
    LaunchedEffect(id) { model.loadStudent(id) }
    LaunchedEffect(model.saved) { if (model.saved) onBack() }
    StudentFormContent(id, model.studentInput, model::editStudent, model.busy, model.loaded, model.error, onBack) {
        model.saveStudent(id)
    }
}

@Composable
private fun StudentFormContent(
    id: Int, input: StudentInput, onInput: (StudentInput) -> Unit, busy: Boolean, loaded: Boolean,
    error: String?, onBack: () -> Unit, onSave: () -> Unit,
) = AdminFormContent(if (id == 0) "Add Student" else "Edit Student", busy, loaded, error, onBack, onSave) {
    Text("Student Information", style = MaterialTheme.typography.titleMedium)
    AdminField("Student Code *", input.studentCode, { onInput(input.copy(studentCode = it)) }, !busy)
    AdminField("Full Name *", input.fullname, { onInput(input.copy(fullname = it)) }, !busy)
    AdminField("Email *", input.email, { onInput(input.copy(email = it)) }, !busy, KeyboardType.Email)
    AdminField("Phone Number *", input.phone, { onInput(input.copy(phone = it)) }, !busy, KeyboardType.Phone)
    Text("Login Account • Student", style = MaterialTheme.typography.titleMedium)
    AdminField("Username *", input.username, { onInput(input.copy(username = it)) }, !busy)
    AdminField(
        if (id == 0) "Password *" else "New Password", input.password,
        { onInput(input.copy(password = it)) }, !busy, KeyboardType.Password, password = true,
    )
    Text(if (id == 0) "Password must be at least 6 characters. A Student account will be created with this profile." else "Leave password blank to keep the current password; a new password must be at least 6 characters.")
}

@Preview(showBackground = true)
@Composable
private fun StudentFormPreview() {
    Group6ApplicationTheme(dynamicColor = false) { StudentFormContent(0, StudentInput(), {}, false, true, null, {}, {}) }
}
