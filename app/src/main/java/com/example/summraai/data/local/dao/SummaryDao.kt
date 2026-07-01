package com.example.summraai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.summraai.data.local.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
    fun getAllSummaries(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    fun getFavoriteSummaries(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE id = :id")
    suspend fun getSummaryById(id: String): SummaryEntity?

    @Query("SELECT * FROM summaries WHERE id = :id")
    fun getSummaryByIdFlow(id: String): Flow<SummaryEntity?>

    @Query("SELECT * FROM summaries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSummaries(query: String): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Update
    suspend fun update(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM summaries")
    suspend fun deleteAll()

    @Query("UPDATE summaries SET isBookmarked = NOT isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: String)

    @Query("UPDATE summaries SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)
}
