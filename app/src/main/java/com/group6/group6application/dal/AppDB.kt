package com.group6.group6application.dal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.Student
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.entities.Subject

@Database(
    entities = [Account::class, Student::class, Subject::class, StudySchedule::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDB : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studyScheduleDao(): StudyScheduleDao

    suspend fun seedIfEmpty() {
        withTransaction {
            if (accountDao().count() == 0) {
                accountDao().insert(Account(username = "admin", password = "admin", role = Role.ADMIN))
                val accountId = accountDao().insert(Account(username = "student1", password = "123456", role = Role.STUDENT))
                studentDao().insert(
                    Student(studentCode = "SV001", fullname = "Nguyễn Văn A", email = "a@example.com", phone = "0900000001", accountId = accountId.toInt())
                )
                subjectDao().insert(Subject(subjectCode = "MOB101", subjectName = "Lập trình di động", description = "Android với Jetpack Compose", credits = 3))
                subjectDao().insert(Subject(subjectCode = "DBI202", subjectName = "Cơ sở dữ liệu", description = "SQL và cơ sở dữ liệu quan hệ", credits = 3))
                subjectDao().insert(Subject(subjectCode = "PRF192", subjectName = "Nhập môn lập trình", description = "Cơ bản về lập trình", credits = 2))
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun get(context: Context): AppDB = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDB::class.java, "group6.db")
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}
