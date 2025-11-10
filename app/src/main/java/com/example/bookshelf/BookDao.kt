package com.example.bookshelf

import androidx.room.*

@Dao
interface BookDao {
    @Query("SELECT * FROM BookEntity ORDER BY title COLLATE NOCASE")
    suspend fun getAll(): List<BookEntity>

    @Insert
    suspend fun insertAll(items: List<BookEntity>): List<Long>

    @Update
    suspend fun update(item: BookEntity)

    @Delete
    suspend fun delete(item: BookEntity)

    @Query("DELETE FROM BookEntity")
    suspend fun deleteAll()
}
