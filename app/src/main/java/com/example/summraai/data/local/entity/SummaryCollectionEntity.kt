package com.example.summraai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.summraai.domain.model.SummaryCollection

@Entity(tableName = "collections")
data class SummaryCollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val createdAt: Long
)

fun SummaryCollectionEntity.toDomain(count: Int): SummaryCollection = SummaryCollection(
    id = id,
    name = name,
    description = description,
    summaryCount = count,
    createdAt = createdAt
)

fun SummaryCollection.toEntity(): SummaryCollectionEntity = SummaryCollectionEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt
)
