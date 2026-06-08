package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_items")
data class QuoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long,
    val productId: Long? = null,
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double
)
