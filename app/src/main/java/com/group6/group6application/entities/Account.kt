package com.group6.group6application.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object Role {
    const val ADMIN = "Admin"
    const val STUDENT = "Student"
}

object ScheduleStatus {
    const val PENDING = "Pending"
    const val CONFIRMED = "Confirmed"
    const val CANCELLED = "Cancelled"
    val ALL = listOf(PENDING, CONFIRMED, CANCELLED)

    fun label(status: String): String = when (status) {
        PENDING -> "Chờ xác nhận"
        CONFIRMED -> "Đã xác nhận"
        CANCELLED -> "Đã hủy"
        else -> status
    }
}

@Entity(tableName = "Account", indices = [Index(value = ["username"], unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val role: String,
)
