package com.group6.group6application.dal

import android.content.Context
import androidx.core.content.edit
import com.group6.group6application.entities.Account

class SessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.contains(KEY_ACCOUNT_ID)
    val accountId: Int get() = prefs.getInt(KEY_ACCOUNT_ID, -1)
    val username: String get() = prefs.getString(KEY_USERNAME, "") ?: ""
    val role: String get() = prefs.getString(KEY_ROLE, "") ?: ""
    val studentId: Int get() = prefs.getInt(KEY_STUDENT_ID, -1)

    fun save(account: Account, studentId: Int = -1) = prefs.edit {
        putInt(KEY_ACCOUNT_ID, account.id)
        putString(KEY_USERNAME, account.username)
        putString(KEY_ROLE, account.role)
        putInt(KEY_STUDENT_ID, studentId)
    }

    fun logout() = prefs.edit { clear() }

    private companion object {
        const val KEY_ACCOUNT_ID = "accountId"
        const val KEY_USERNAME = "username"
        const val KEY_ROLE = "role"
        const val KEY_STUDENT_ID = "studentId"
    }
}
