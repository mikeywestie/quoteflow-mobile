package com.mikeywestie.quoteflow.feature.quotes

data class QuoteDraftLine(
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val productId: Long? = null
) {
    val lineTotal: Double
        get() = quantity * unitPrice
}