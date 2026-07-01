package com.example.summraai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.summraai.data.local.entity.CollectionSummaryCrossRef
import com.example.summraai.data.local.entity.SummaryCollectionEntity
import com.example.summraai.data.local.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<SummaryCollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: String): SummaryCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: SummaryCollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE collections SET name = :name, description = :description WHERE id = :id")
    suspend fun update(id: String, name: String, description: String)

    @Query("SELECT COUNT(*) FROM collection_summary_cross_ref WHERE collectionId = :collectionId")
    suspend fun getSummaryCount(collectionId: String): Int

    @Query("""
        SELECT s.* FROM summaries s
        INNER JOIN collection_summary_cross_ref c ON s.id = c.summaryId
        WHERE c.collectionId = :collectionId
        ORDER BY s.createdAt DESC
    """)
    fun getSummariesInCollection(collectionId: String): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSummaryToCollection(crossRef: CollectionSummaryCrossRef)

    @Query("DELETE FROM collection_summary_cross_ref WHERE collectionId = :collectionId AND summaryId = :summaryId")
    suspend fun removeSummaryFromCollection(collectionId: String, summaryId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM collection_summary_cross_ref WHERE collectionId = :collectionId AND summaryId = :summaryId)")
    suspend fun isSummaryInCollection(collectionId: String, summaryId: String): Boolean
}
