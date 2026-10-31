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
fun AdminHomeActivity(onStudents: () -> Unit, onSubjects: () -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Trang quản trị", style = MaterialTheme.typography.headlineMedium)
        Text("Quản lý sinh viên và môn học của ứng dụng lịch học.", style = MaterialTheme.typography.bodyLarge)
        Card(onClick = onStudents, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quản lý sinh viên", style = MaterialTheme.typography.titleLarge)
                Text("Xem, tìm kiếm, cập nhật hồ sơ sinh viên và tạo tài khoản Student.")
            }
        }
        Card(onClick = onSubjects, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quản lý môn học", style = MaterialTheme.typography.titleLarge)
                Text("Quản lý mã môn, tên môn, mô tả và số tín chỉ.")
            }
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Đăng xuất") }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminHomePreview() {
    Group6ApplicationTheme(dynamicColor = false) { AdminHomeActivity({}, {}, {}) }
}

