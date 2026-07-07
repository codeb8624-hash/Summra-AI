package com.example.summraai.data.repository

import android.util.Log
import com.example.summraai.data.local.dao.CollectionDao
import com.example.summraai.data.local.dao.SummaryDao
import com.example.summraai.data.local.entity.CollectionSummaryCrossRef
import com.example.summraai.data.local.entity.toDomain
import com.example.summraai.data.local.entity.toEntity
import com.example.summraai.domain.model.HistoryItem
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryCollection
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.model.SummaryType
import com.example.summraai.domain.repository.HistoryRepository
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomSummaryRepository(
    private val summaryDao: SummaryDao,
    private val collectionDao: CollectionDao
) : SummaryRepository, HistoryRepository {

    override suspend fun getSummaries(): List<Summary> {
        val summaries = summaryDao.getAllSummaries().first().map { it.toDomain() }
        Log.d("PIPELINE", "getSummaries: count=${summaries.size}")
        return summaries
    }

    fun getAllSummariesFlow(): Flow<List<Summary>> =
        summaryDao.getAllSummaries().map { list -> list.map { it.toDomain() } }

    fun getFavoriteSummariesFlow(): Flow<List<Summary>> =
        summaryDao.getFavoriteSummaries().map { list -> list.map { it.toDomain() } }

    override fun searchSummariesFlow(query: String): Flow<List<Summary>> =
        summaryDao.searchSummaries(query).map { list -> list.map { it.toDomain() } }

    override fun getSummaryByIdFlow(id: String): Flow<Summary?> =
        summaryDao.getSummaryByIdFlow(id).map { it?.toDomain() }

    override suspend fun getSummaryById(id: String): Summary? =
        summaryDao.getSummaryById(id)?.toDomain()

    override suspend fun saveSummary(summary: Summary) {
        Log.d("PIPELINE", "saveSummary: id=${summary.id}, type=${summary.type}, title=${summary.title}")
        summaryDao.insert(summary.toEntity())
    }

    override suspend fun deleteSummary(id: String) =
        summaryDao.deleteById(id)

    override suspend fun toggleBookmark(id: String) =
        summaryDao.toggleBookmark(id)

    override suspend fun getSummariesByStyle(style: SummaryStyle): List<Summary> =
        summaryDao.getAllSummaries().first()
            .filter { it.style == style }
            .map { it.toDomain() }

    suspend fun updateTitle(id: String, title: String) =
        summaryDao.updateTitle(id, title)

    override fun getHistory(): Flow<List<HistoryItem>> =
        summaryDao.getAllSummaries().map { list ->
            list.map { entity ->
                HistoryItem(
                    id = entity.id,
                    title = entity.title,
                    summary = entity.content,
                    type = entity.type,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(entity.createdAt)),
                    timestamp = entity.createdAt
                )
            }
        }

    override fun getSummariesFlow(): Flow<List<Summary>> = getAllSummariesFlow()

    override suspend fun addToHistory(item: HistoryItem) {
        Log.d("PIPELINE", "addToHistory: id=${item.id}, type=${item.type}, title=${item.title}")
        val summary = Summary(
            id = item.id,
            title = item.title,
            content = item.summary,
            type = item.type,
            source = "",
            createdAt = item.timestamp,
            wordCount = item.summary.split("\\s+".toRegex()).size,
            tags = emptyList(),
            isBookmarked = false,
            style = SummaryStyle.CONCISE
        )
        summaryDao.insert(summary.toEntity())
    }

    override suspend fun removeFromHistory(id: String) =
        summaryDao.deleteById(id)

    override suspend fun clearHistory() =
        summaryDao.deleteAll()

    fun getAllCollectionsFlow(): Flow<List<SummaryCollection>> =
        collectionDao.getAllCollections().map { entities ->
            entities.map { entity ->
                val count = collectionDao.getSummaryCount(entity.id)
                entity.toDomain(count)
            }
        }

    suspend fun createCollection(collection: SummaryCollection) {
        Log.d("PIPELINE", "createCollection: id=${collection.id}, name=${collection.name}")
        collectionDao.insert(collection.toEntity())
    }

    suspend fun updateCollection(id: String, name: String, description: String) =
        collectionDao.update(id, name, description)

    suspend fun deleteCollection(id: String) =
        collectionDao.deleteById(id)

    fun getSummariesInCollectionFlow(collectionId: String): Flow<List<Summary>> =
        collectionDao.getSummariesInCollection(collectionId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun addSummaryToCollection(collectionId: String, summaryId: String) =
        collectionDao.addSummaryToCollection(CollectionSummaryCrossRef(collectionId, summaryId))

    suspend fun removeSummaryFromCollection(collectionId: String, summaryId: String) =
        collectionDao.removeSummaryFromCollection(collectionId, summaryId)

    suspend fun isSummaryInCollection(collectionId: String, summaryId: String): Boolean =
        collectionDao.isSummaryInCollection(collectionId, summaryId)
}
