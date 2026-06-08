package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_templates")
data class QuoteTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val templateName: String,
    val category: String = "General",
    val description: String = "",
    val exampleQuotes: String = "",
    val confidence: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)