package com.group6.group6application.entities

import androidx.room.Embedded

data class ScheduleWithSubject(
    @Embedded val schedule: StudySchedule,
    val subjectCode: String,
    val subjectName: String,
)
