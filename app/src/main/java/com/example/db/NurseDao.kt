package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NurseDao {
    // Bookmarks
    @Query("SELECT resourceId FROM bookmarks")
    fun getAllBookmarkedIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE resourceId = :id")
    suspend fun removeBookmark(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE resourceId = :id)")
    suspend fun isBookmarked(id: String): Boolean

    // Quiz History
    @Query("SELECT * FROM quiz_history ORDER BY date DESC")
    fun getAllQuizHistory(): Flow<List<QuizHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizHistoryEntity)

    @Query("DELETE FROM quiz_history")
    suspend fun clearQuizHistory()

    // Custom Resources
    @Query("SELECT * FROM custom_resources ORDER BY createdAt DESC")
    fun getAllCustomResources(): Flow<List<CustomResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomResource(resource: CustomResourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomResources(resources: List<CustomResourceEntity>)

    @Delete
    suspend fun deleteCustomResource(resource: CustomResourceEntity)
}
