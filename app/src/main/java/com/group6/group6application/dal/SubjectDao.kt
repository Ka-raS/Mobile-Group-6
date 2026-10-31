package com.group6.group6application.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group6.group6application.entities.Subject
import kotlinx.coroutines.flow.Flow

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
