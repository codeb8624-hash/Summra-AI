package com.example.summraai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.model.SummaryType

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val type: SummaryType,
    val source: String,
    val createdAt: Long,
    val wordCount: Int,
    val tags: List<String>,
    val isBookmarked: Boolean,
    val style: SummaryStyle,
    val lastViewedAt: Long? = null
)

fun SummaryEntity.toDomain(): Summary = Summary(
    id = id,
    title = title,
    content = content,
    type = type,
    source = source,
    createdAt = createdAt,
    wordCount = wordCount,
    tags = tags,
    isBookmarked = isBookmarked,
    style = style
)

fun Summary.toEntity(): SummaryEntity = SummaryEntity(
    id = id,
    title = title,
    content = content,
    type = type,
    source = source,
    createdAt = createdAt,
    wordCount = wordCount,
    tags = tags,
    isBookmarked = isBookmarked,
    style = style,
    lastViewedAt = createdAt
)
