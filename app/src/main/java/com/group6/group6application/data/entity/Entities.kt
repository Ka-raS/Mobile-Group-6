package com.group6.group6application.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
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
}

@Entity(tableName = "Account", indices = [Index(value = ["username"], unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val role: String,
)

@Entity(
    tableName = "Student",
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = ["id"],
        childColumns = ["accountId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["studentCode"], unique = true), Index("accountId")],
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentCode: String,
    val fullname: String,
    val email: String,
    val phone: String,
    val accountId: Int,
)

@Entity(tableName = "Subject", indices = [Index(value = ["subjectCode"], unique = true)])
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectCode: String,
    val subjectName: String,
    val description: String,
    val credits: Int,
)

/** studyDate: "yyyy-MM-dd", startTime/endTime: "HH:mm" */
@Entity(
    tableName = "StudySchedule",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("studentId"), Index("subjectId")],
)
data class StudySchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val subjectId: Int,
    val studyDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val status: String = ScheduleStatus.PENDING,
)
