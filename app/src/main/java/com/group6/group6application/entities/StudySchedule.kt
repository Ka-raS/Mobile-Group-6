package com.group6.group6application.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
