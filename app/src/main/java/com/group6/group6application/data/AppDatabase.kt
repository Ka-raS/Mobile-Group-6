package com.group6.group6application.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.group6.group6application.data.dao.AccountDao
import com.group6.group6application.data.dao.StudentDao
import com.group6.group6application.data.dao.StudyScheduleDao
import com.group6.group6application.data.dao.SubjectDao
import com.group6.group6application.data.entity.Account
import com.group6.group6application.data.entity.Student
import com.group6.group6application.data.entity.StudySchedule
import com.group6.group6application.data.entity.Subject

@Database(
    entities = [Account::class, Student::class, Subject::class, StudySchedule::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studyScheduleDao(): StudyScheduleDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "group6.db")
                .addCallback(SeedCallback)
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }

        /** Sample data: admin/admin, student1/123456 and a few subjects. */
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
