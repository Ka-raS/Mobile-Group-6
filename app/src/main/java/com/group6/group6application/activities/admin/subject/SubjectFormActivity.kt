package com.group6.group6application.activities.admin.subject

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group6.group6application.activities.admin.*
import com.group6.group6application.activities.theme.Group6ApplicationTheme

@Composable
fun SubjectFormActivity(id: Int, onBack: () -> Unit, model: AdminViewModel = viewModel()) {
    LaunchedEffect(id) { model.loadSubject(id) }
    LaunchedEffect(model.saved) { if (model.saved) onBack() }
    SubjectFormContent(id, model.subjectInput, model::editSubject, model.busy, model.loaded, model.error, onBack) {
        model.saveSubject(id)
    }
}

@Composable
private fun SubjectFormContent(
    id: Int, input: SubjectInput, onInput: (SubjectInput) -> Unit, busy: Boolean, loaded: Boolean,
    error: String?, onBack: () -> Unit, onSave: () -> Unit,
) = AdminFormContent(if (id == 0) "Thêm môn học" else "Sửa môn học", busy, loaded, error, onBack, onSave) {
    AdminField("Mã môn học *", input.subjectCode, { onInput(input.copy(subjectCode = it)) }, !busy)
    AdminField("Tên môn học *", input.subjectName, { onInput(input.copy(subjectName = it)) }, !busy)
    AdminField("Mô tả", input.description, { onInput(input.copy(description = it)) }, !busy, multiline = true)
    AdminField("Số tín chỉ *", input.credits, { onInput(input.copy(credits = it)) }, !busy, KeyboardType.Number)
    Text("Các trường có dấu * là bắt buộc. Số tín chỉ phải là số nguyên dương.")
}

@Preview(showBackground = true)
@Composable
private fun SubjectFormPreview() {
    Group6ApplicationTheme(dynamicColor = false) { SubjectFormContent(0, SubjectInput(), {}, false, true, null, {}, {}) }
}

