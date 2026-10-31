package com.group6.group6application.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group6.group6application.data.entity.Account
import com.group6.group6application.data.entity.Student
import com.group6.group6application.data.entity.StudySchedule
import com.group6.group6application.data.entity.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM Account WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): Account?

    @Query("SELECT * FROM Account WHERE id = :id")
    suspend fun getById(id: Int): Account?

    @Query("SELECT * FROM Account WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): Account?
}

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM Student ORDER BY fullname")
    fun getAll(): Flow<List<Student>>

    @Query(
        "SELECT * FROM Student WHERE fullname LIKE '%' || :q || '%' " +
            "OR studentCode LIKE '%' || :q || '%' OR email LIKE '%' || :q || '%' " +
            "OR phone LIKE '%' || :q || '%' ORDER BY fullname"
    )
    fun search(q: String): Flow<List<Student>>

    @Query("SELECT * FROM Student WHERE id = :id")
    suspend fun getById(id: Int): Student?

    @Query("SELECT * FROM Student WHERE accountId = :accountId LIMIT 1")
    suspend fun getByAccountId(accountId: Int): Student?
}

@Dao
interface SubjectDao {
    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("SELECT * FROM Subject ORDER BY subjectName")
    fun getAll(): Flow<List<Subject>>

    @Query(
        "SELECT * FROM Subject WHERE subjectName LIKE '%' || :q || '%' " +
            "OR subjectCode LIKE '%' || :q || '%' ORDER BY subjectName"
    )
    fun search(q: String): Flow<List<Subject>>

    @Query("SELECT * FROM Subject WHERE id = :id")
    suspend fun getById(id: Int): Subject?
}

/** StudySchedule joined with its Subject, for display. */
data class ScheduleWithSubject(
    @Embedded val schedule: StudySchedule,
    val subjectCode: String,
    val subjectName: String,
)

@Dao
interface StudyScheduleDao {
    @Insert
    suspend fun insert(schedule: StudySchedule): Long

    @Update
    suspend fun update(schedule: StudySchedule)

    @Delete
    suspend fun delete(schedule: StudySchedule)

    @Query("UPDATE StudySchedule SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("SELECT * FROM StudySchedule WHERE id = :id")
    suspend fun getById(id: Int): StudySchedule?

    @Query(
        "SELECT ss.*, s.subjectCode, s.subjectName FROM StudySchedule ss " +
            "JOIN Subject s ON s.id = ss.subjectId WHERE ss.studentId = :studentId " +
            "ORDER BY ss.studyDate, ss.startTime"
    )
    fun getByStudent(studentId: Int): Flow<List<ScheduleWithSubject>>

    /** Search by subject name/code or location, optionally filtered by status (null = all). */
    @Query(
        "SELECT ss.*, s.subjectCode, s.subjectName FROM StudySchedule ss " +
            "JOIN Subject s ON s.id = ss.subjectId WHERE ss.studentId = :studentId " +
            "AND (s.subjectName LIKE '%' || :q || '%' OR s.subjectCode LIKE '%' || :q || '%' " +
            "OR ss.location LIKE '%' || :q || '%') " +
            "AND (:status IS NULL OR ss.status = :status) " +
            "ORDER BY ss.studyDate, ss.startTime"
    )
    fun search(studentId: Int, q: String, status: String?): Flow<List<ScheduleWithSubject>>

    /** Schedules between two dates inclusive ("yyyy-MM-dd"), for calendar day/week/month views. */
    @Query(
        "SELECT ss.*, s.subjectCode, s.subjectName FROM StudySchedule ss " +
            "JOIN Subject s ON s.id = ss.subjectId WHERE ss.studentId = :studentId " +
            "AND ss.studyDate BETWEEN :fromDate AND :toDate " +
            "ORDER BY ss.studyDate, ss.startTime"
    )
    fun getInRange(studentId: Int, fromDate: String, toDate: String): Flow<List<ScheduleWithSubject>>

    /**
     * Number of non-cancelled schedules of the student overlapping [start, end) on [date].
     * Pass excludeId = id of the schedule being edited (0 when creating). > 0 means conflict.
     */
    @Query(
        "SELECT COUNT(*) FROM StudySchedule WHERE studentId = :studentId AND studyDate = :date " +
            "AND status != 'Cancelled' AND startTime < :end AND endTime > :start AND id != :excludeId"
    )
    suspend fun countOverlap(studentId: Int, date: String, start: String, end: String, excludeId: Int = 0): Int
}
