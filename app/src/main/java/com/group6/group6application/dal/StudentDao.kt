package com.group6.group6application.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group6.group6application.entities.Student
import kotlinx.coroutines.flow.Flow

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
