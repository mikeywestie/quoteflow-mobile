package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String = "",
    val name: String,
    val description: String = "",
    val category: String = "General",
    val unitPrice: Double,
    val unit: String = "Each",
    val supplier: String = "",
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
