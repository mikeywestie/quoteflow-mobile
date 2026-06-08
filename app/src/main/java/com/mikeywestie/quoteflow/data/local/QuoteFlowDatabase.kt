package com.mikeywestie.quoteflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mikeywestie.quoteflow.data.local.dao.CompanySettingsDao
import com.mikeywestie.quoteflow.data.local.dao.CustomerDao
import com.mikeywestie.quoteflow.data.local.dao.ProductDao
import com.mikeywestie.quoteflow.data.local.dao.QuoteDao
import com.mikeywestie.quoteflow.data.local.dao.QuoteTemplateDao
import com.mikeywestie.quoteflow.data.local.entity.CompanySettings
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.Quote
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem
import com.mikeywestie.quoteflow.data.local.entity.QuoteTemplate
import com.mikeywestie.quoteflow.data.local.dao.TemplateItemDao
import com.mikeywestie.quoteflow.data.local.entity.TemplateItem

@Database(
    entities = [
        Product::class,
        Customer::class,
        Quote::class,
        QuoteItem::class,
        CompanySettings::class,
        QuoteTemplate::class,
        TemplateItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuoteFlowDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun quoteDao(): QuoteDao
    abstract fun companySettingsDao(): CompanySettingsDao
    abstract fun quoteTemplateDao(): QuoteTemplateDao
    abstract fun templateItemDao(): TemplateItemDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteFlowDatabase? = null

        fun getDatabase(context: Context): QuoteFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuoteFlowDatabase::class.java,
                    "quoteflow.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}