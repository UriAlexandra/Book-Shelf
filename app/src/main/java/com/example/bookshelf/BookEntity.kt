package com.example.bookshelf

import androidx.room.Entity
import androidx.room.PrimaryKey

// Egy könyv rekord a táblában
@Entity
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // PK
    val title: String,         // Cím
    val author: String,        // Szerző
    val genre: String,         // Műfaj
    val year: Int,             // Kiadási év
    val isRead: Boolean = false // Olvasott-e
)
