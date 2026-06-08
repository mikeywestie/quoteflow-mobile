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

    @Query("SELECT * FROM quotes WHERE id = :quoteId LIMIT 1")
    suspend fun getQuoteById(quoteId: Long): Quote?

    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY id")
    fun getQuoteItems(quoteId: Long): Flow<List<QuoteItem>>

    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY id")
    suspend fun getQuoteItemsOnce(quoteId: Long): List<QuoteItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuote(quote: Quote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: QuoteItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItems(items: List<QuoteItem>)

    @Delete
    suspend fun deleteItem(item: QuoteItem)

    @Query("DELETE FROM quote_items WHERE quoteId = :quoteId")
    suspend fun deleteItemsForQuote(quoteId: Long)
}