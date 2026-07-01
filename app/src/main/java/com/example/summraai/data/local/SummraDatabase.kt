package com.example.summraai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.summraai.data.local.dao.CollectionDao
import com.example.summraai.data.local.dao.SummaryDao
import com.example.summraai.data.local.entity.CollectionSummaryCrossRef
import com.example.summraai.data.local.entity.Converters
import com.example.summraai.data.local.entity.SummaryCollectionEntity
import com.example.summraai.data.local.entity.SummaryEntity

@Database(
    entities = [
        SummaryEntity::class,
        SummaryCollectionEntity::class,
        CollectionSummaryCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SummraDatabase : RoomDatabase() {
    abstract fun summaryDao(): SummaryDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: SummraDatabase? = null

        fun getInstance(context: Context): SummraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SummraDatabase::class.java,
                    "summra_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
