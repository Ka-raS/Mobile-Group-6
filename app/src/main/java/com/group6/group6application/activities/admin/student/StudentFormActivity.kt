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
fun StudentFormActivity(id: Int, onBack: () -> Unit, model: AdminViewModel = viewModel()) {
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
) = AdminFormContent(if (id == 0) "Thêm sinh viên" else "Sửa sinh viên", busy, loaded, error, onBack, onSave) {
    Text("Thông tin sinh viên", style = MaterialTheme.typography.titleMedium)
    AdminField("Mã sinh viên *", input.studentCode, { onInput(input.copy(studentCode = it)) }, !busy)
    AdminField("Họ tên *", input.fullname, { onInput(input.copy(fullname = it)) }, !busy)
    AdminField("Email *", input.email, { onInput(input.copy(email = it)) }, !busy, KeyboardType.Email)
    AdminField("Số điện thoại *", input.phone, { onInput(input.copy(phone = it)) }, !busy, KeyboardType.Phone)
    Text("Tài khoản đăng nhập • Student", style = MaterialTheme.typography.titleMedium)
    AdminField("Tên đăng nhập *", input.username, { onInput(input.copy(username = it)) }, !busy)
    AdminField(
        if (id == 0) "Mật khẩu *" else "Mật khẩu mới", input.password,
        { onInput(input.copy(password = it)) }, !busy, KeyboardType.Password, password = true,
    )
    Text(if (id == 0) "Mật khẩu có ít nhất 6 ký tự. Tài khoản Student sẽ được tạo cùng hồ sơ này." else "Để trống mật khẩu để giữ mật khẩu cũ; mật khẩu mới phải có ít nhất 6 ký tự.")
}

@Preview(showBackground = true)
@Composable
private fun StudentFormPreview() {
    Group6ApplicationTheme(dynamicColor = false) { StudentFormContent(0, StudentInput(), {}, false, true, null, {}, {}) }
}

