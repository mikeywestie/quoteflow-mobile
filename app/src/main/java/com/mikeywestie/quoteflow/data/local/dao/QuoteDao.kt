package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.*
import com.mikeywestie.quoteflow.data.local.entity.Quote
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getQuotes(): Flow<List<Quote>>

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun countQuotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuote(quote: Quote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: QuoteItem): Long
}
