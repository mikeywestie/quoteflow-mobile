package com.mikeywestie.quoteflow.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val QUOTES = "quotes"
    const val SETTINGS = "settings"
    const val IMPORT_DATA = "import-data"

    const val QUOTE_DETAILS = "quote-details"
    const val QUOTE_ID_ARGUMENT = "quoteId"

    fun quoteDetails(quoteId: Long): String {
        return "$QUOTE_DETAILS/$quoteId"
    }
}