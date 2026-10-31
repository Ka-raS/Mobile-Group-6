package com.group6.group6application.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Role
import kotlinx.coroutines.launch

@Composable
fun LoginActivity(onLoggedIn: (role: String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDB.get(context) }
    val session = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Lịch học", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("Tên đăng nhập") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                loading = true
                scope.launch {
                    try {
                        db.seedIfEmpty()
                        val account = db.accountDao().login(username.trim(), password)
                        if (account == null) {
                            error = "Sai tên đăng nhập hoặc mật khẩu"
                            return@launch
                        }
                        val studentId = if (account.role == Role.STUDENT) {
                            db.studentDao().getByAccountId(account.id)?.id ?: -1
                        } else -1
                        if (account.role == Role.STUDENT && studentId <= 0) {
                            error = "Tài khoản chưa có hồ sơ sinh viên"
                            return@launch
                        }
                        session.save(account, studentId)
                        onLoggedIn(account.role)
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Đăng nhập") }
    }
}
