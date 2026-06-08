package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "template_items")
data class TemplateItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val templateId: Long,

    val productId: Long,

    val quantity: Double = 1.0
)