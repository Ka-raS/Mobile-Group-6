package com.group6.group6application.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
