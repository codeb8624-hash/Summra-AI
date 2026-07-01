package com.example.summraai.data.local.entity

import androidx.room.TypeConverter
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.model.SummaryType

class Converters {
    @TypeConverter
    fun fromSummaryType(value: SummaryType): String = value.name

    @TypeConverter
    fun toSummaryType(value: String): SummaryType = SummaryType.valueOf(value)

    @TypeConverter
    fun fromSummaryStyle(value: SummaryStyle): String = value.name

    @TypeConverter
    fun toSummaryStyle(value: String): SummaryStyle = SummaryStyle.valueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(",")
}
