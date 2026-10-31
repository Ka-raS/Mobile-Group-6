package com.group6.group6application.activities.admin.student

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group6.group6application.activities.admin.*
import com.group6.group6application.activities.theme.Group6ApplicationTheme

@Composable
fun StudentListActivity(onBack: () -> Unit, onAdd: () -> Unit, onEdit: (Int) -> Unit, model: AdminViewModel = viewModel()) {
    val students by model.students.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val records = remember(students) {
        students?.map {
            AdminRecord(it.id, it.fullname, "${it.studentCode} • ${it.email}", listOf(
                "Student Code" to it.studentCode, "Full Name" to it.fullname,
                "Email" to it.email, "Phone Number" to it.phone,
            ))
        }
    }
    StudentListContent(records, query, { query = it }, model.busy, model.error, onBack, onAdd, onEdit, model::deleteStudent)
}

@Composable
private fun StudentListContent(
    records: List<AdminRecord>?, query: String, onQuery: (String) -> Unit, busy: Boolean, error: String?,
    onBack: () -> Unit, onAdd: () -> Unit, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit,
) = AdminListContent(
    title = "Student Management", records = records, query = query, onQuery = onQuery,
    searchHint = "Code, name, email, phone number", busy = busy, error = error,
    deleteWarning = "The student's login account and all associated study schedules will also be deleted. This action cannot be undone.",
    onBack = onBack, onAdd = onAdd, onEdit = onEdit, onDelete = onDelete,
)

@Preview(showBackground = true)
@Composable
private fun StudentsPreview() {
    Group6ApplicationTheme(dynamicColor = false) {
        StudentListContent(listOf(AdminRecord(1, "Nguyễn Văn An", "SV001 • an@example.com", emptyList())), "", {}, false, null, {}, {}, {}, {})
    }
}

