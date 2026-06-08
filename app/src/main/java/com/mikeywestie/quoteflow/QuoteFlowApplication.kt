package com.mikeywestie.quoteflow

import android.app.Application
import com.mikeywestie.quoteflow.data.local.QuoteFlowDatabase
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository

class QuoteFlowApplication : Application() {

    val database by lazy {
        QuoteFlowDatabase.getDatabase(this)
    }

    val repository by lazy {
        QuoteFlowRepository(
            database.productDao(),
            database.customerDao(),
            database.quoteDao(),
            database.companySettingsDao(),
            database.quoteTemplateDao(),
            database.templateItemDao()
        )
    }
}