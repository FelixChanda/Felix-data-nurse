package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val resourceId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resourceId: String,
    val resourceTitle: String,
    val score: Int,
    val total: Int,
    val percentage: Int,
    val mode: String,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_resources")
data class CustomResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val domain: String,
    val yearLevel: String,
    val description: String,
    val contentText: String,
    val tagsCommaSeparated: String,
    val createdAt: Long = System.currentTimeMillis()
)
