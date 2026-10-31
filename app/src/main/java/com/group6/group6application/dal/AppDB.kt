package com.group6.group6application.dal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.group6.group6application.dal.AccountDao
import com.group6.group6application.dal.StudentDao
import com.group6.group6application.dal.StudyScheduleDao
import com.group6.group6application.dal.SubjectDao
import com.group6.group6application.entities.Account
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

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun get(context: Context): AppDB = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDB::class.java, "group6.db")
                .addCallback(SeedCallback)
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }

        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT INTO Account(id, username, password, role) VALUES (1, 'admin', 'admin', 'Admin')")
                db.execSQL("INSERT INTO Account(id, username, password, role) VALUES (2, 'student1', '123456', 'Student')")
                db.execSQL(
                    "INSERT INTO Student(studentCode, fullname, email, phone, accountId) " +
                        "VALUES ('SV001', 'Nguyen Van A', 'a@example.com', '0900000001', 2)"
                )
                db.execSQL(
                    "INSERT INTO Subject(subjectCode, subjectName, description, credits) VALUES " +
                        "('MOB101', 'Mobile Programming', 'Android with Jetpack Compose', 3), " +
                        "('DBI202', 'Database Systems', 'SQL and relational databases', 3), " +
                        "('PRF192', 'Programming Fundamentals', 'Intro to programming', 2)"
                )
            }
        }
    }
}
