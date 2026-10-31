package com.group6.group6application.activities.admin.subject

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group6.group6application.activities.admin.*
import com.group6.group6application.activities.theme.Group6ApplicationTheme

@Composable
fun SubjectListActivity(onBack: () -> Unit, onAdd: () -> Unit, onEdit: (Int) -> Unit, model: AdminViewModel = viewModel()) {
    val subjects by model.subjects.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val records = remember(subjects) {
        subjects?.map {
            AdminRecord(it.id, it.subjectName, "${it.subjectCode} • ${it.credits} ${if (it.credits == 1) "credit" else "credits"}", listOf(
                "Subject Code" to it.subjectCode, "Subject Name" to it.subjectName,
                "Description" to it.description, "Credits" to it.credits.toString(),
            ))
        }
    }
    SubjectListContent(records, query, { query = it }, model.busy, model.error, onBack, onAdd, onEdit, model::deleteSubject)
}

@Composable
private fun SubjectListContent(
    records: List<AdminRecord>?, query: String, onQuery: (String) -> Unit, busy: Boolean, error: String?,
    onBack: () -> Unit, onAdd: () -> Unit, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit,
) = AdminListContent(
    title = "Subject Management", records = records, query = query, onQuery = onQuery,
    searchHint = "Subject code, name, or description", busy = busy, error = error,
    deleteWarning = "All study schedules associated with this subject will also be deleted. This action cannot be undone.",
    onBack = onBack, onAdd = onAdd, onEdit = onEdit, onDelete = onDelete,
)

@Preview(showBackground = true)
@Composable
private fun SubjectsPreview() {
    Group6ApplicationTheme(dynamicColor = false) {
        SubjectListContent(listOf(AdminRecord(1, "Mobile Programming", "MOB101 • 3 credits", emptyList())), "", {}, false, null, {}, {}, {}, {})
    }
}

