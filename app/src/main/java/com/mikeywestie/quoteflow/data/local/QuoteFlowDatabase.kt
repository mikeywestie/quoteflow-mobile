package com.mikeywestie.quoteflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mikeywestie.quoteflow.data.local.dao.CustomerDao
import com.mikeywestie.quoteflow.data.local.dao.ProductDao
import com.mikeywestie.quoteflow.data.local.dao.QuoteDao
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.Quote
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem

@Database(entities = [Product::class, Customer::class, Quote::class, QuoteItem::class], version = 1, exportSchema = false)
abstract class QuoteFlowDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile private var INSTANCE: QuoteFlowDatabase? = null
        fun getDatabase(context: Context): QuoteFlowDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, QuoteFlowDatabase::class.java, "quoteflow.db").build().also { INSTANCE = it }
        }
    }
}
