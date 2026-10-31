package com.group6.group6application.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group6.group6application.entities.ScheduleWithSubject
import com.group6.group6application.entities.StudySchedule
import kotlinx.coroutines.flow.Flow

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

    @Query(
        "SELECT ss.*, s.subjectCode, s.subjectName FROM StudySchedule ss " +
            "JOIN Subject s ON s.id = ss.subjectId WHERE ss.studentId = :studentId " +
            "AND (s.subjectName LIKE '%' || :q || '%' OR s.subjectCode LIKE '%' || :q || '%' " +
            "OR ss.location LIKE '%' || :q || '%') " +
            "AND (:status IS NULL OR ss.status = :status) " +
            "ORDER BY ss.studyDate, ss.startTime"
    )
    fun search(studentId: Int, q: String, status: String?): Flow<List<ScheduleWithSubject>>

    @Query(
        "SELECT ss.*, s.subjectCode, s.subjectName FROM StudySchedule ss " +
            "JOIN Subject s ON s.id = ss.subjectId WHERE ss.studentId = :studentId " +
            "AND ss.studyDate BETWEEN :fromDate AND :toDate " +
            "ORDER BY ss.studyDate, ss.startTime"
    )
    fun getInRange(studentId: Int, fromDate: String, toDate: String): Flow<List<ScheduleWithSubject>>

    @Query(
        "SELECT COUNT(*) FROM StudySchedule WHERE studentId = :studentId AND studyDate = :date " +
            "AND status != 'Cancelled' AND startTime < :end AND endTime > :start AND id != :excludeId"
    )
    suspend fun countOverlap(studentId: Int, date: String, start: String, end: String, excludeId: Int = 0): Int
}
