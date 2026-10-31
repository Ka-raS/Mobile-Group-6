package com.group6.group6application.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Subject", indices = [Index(value = ["subjectCode"], unique = true)])
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectCode: String,
    val subjectName: String,
    val description: String,
    val credits: Int,
)
