package com.example.summraai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_summary_cross_ref",
    primaryKeys = ["collectionId", "summaryId"],
    foreignKeys = [
        ForeignKey(
            entity = SummaryCollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SummaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["summaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("summaryId")
    ]
)
data class CollectionSummaryCrossRef(
    val collectionId: String,
    val summaryId: String
)
