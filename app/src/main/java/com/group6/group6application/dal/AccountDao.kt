package com.group6.group6application.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group6.group6application.entities.Account

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

    @Query("SELECT COUNT(*) FROM Account")
    suspend fun count(): Int
}
