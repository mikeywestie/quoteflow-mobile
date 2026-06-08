package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteNumber: String,
    val customerId: Long,
    val status: String = "Draft",
    val notes: String = "",
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
