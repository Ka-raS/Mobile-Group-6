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
            AdminRecord(it.id, it.subjectName, "${it.subjectCode} • ${it.credits} tín chỉ", listOf(
                "Mã môn học" to it.subjectCode, "Tên môn học" to it.subjectName,
                "Mô tả" to it.description, "Số tín chỉ" to it.credits.toString(),
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
    title = "Quản lý môn học", records = records, query = query, onQuery = onQuery,
    searchHint = "Mã môn, tên môn hoặc mô tả", busy = busy, error = error,
    deleteWarning = "Toàn bộ lịch học của môn này cũng sẽ bị xóa. Không thể hoàn tác.",
    onBack = onBack, onAdd = onAdd, onEdit = onEdit, onDelete = onDelete,
)

@Preview(showBackground = true)
@Composable
private fun SubjectsPreview() {
    Group6ApplicationTheme(dynamicColor = false) {
        SubjectListContent(listOf(AdminRecord(1, "Lập trình di động", "MOB101 • 3 tín chỉ", emptyList())), "", {}, false, null, {}, {}, {}, {})
    }
}

