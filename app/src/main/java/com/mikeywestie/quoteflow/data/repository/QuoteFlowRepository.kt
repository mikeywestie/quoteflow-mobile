package com.mikeywestie.quoteflow.data.repository

import com.mikeywestie.quoteflow.data.local.dao.CompanySettingsDao
import com.mikeywestie.quoteflow.data.local.dao.CustomerDao
import com.mikeywestie.quoteflow.data.local.dao.ProductDao
import com.mikeywestie.quoteflow.data.local.dao.QuoteDao
import com.mikeywestie.quoteflow.data.local.entity.CompanySettings
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.Quote
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem
import kotlinx.coroutines.flow.Flow
import java.time.Year

class QuoteFlowRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val quoteDao: QuoteDao,
    private val companySettingsDao: CompanySettingsDao
) {
    fun products(query: String): Flow<List<Product>> =
        productDao.searchProducts(query)

    suspend fun saveProduct(product: Product) =
        productDao.save(product)

    suspend fun deleteProduct(product: Product) =
        productDao.delete(product)

    fun customers(query: String): Flow<List<Customer>> =
        customerDao.searchCustomers(query)

    suspend fun saveCustomer(customer: Customer) =
        customerDao.save(customer)

    suspend fun deleteCustomer(customer: Customer) =
        customerDao.delete(customer)

    fun quotes(): Flow<List<Quote>> =
        quoteDao.getQuotes()

    fun quoteItems(quoteId: Long): Flow<List<QuoteItem>> =
        quoteDao.getQuoteItems(quoteId)

    suspend fun createDraftQuote(customerId: Long): Long {
        return quoteDao.saveQuote(
            Quote(
                quoteNumber = nextQuoteNumber(),
                customerId = customerId
            )
        )
    }

    suspend fun saveQuoteWithItems(
        customerId: Long,
        notes: String,
        items: List<QuoteItem>
    ): Long {
        val total = items.sumOf { it.lineTotal }

        val quoteId = quoteDao.saveQuote(
            Quote(
                quoteNumber = nextQuoteNumber(),
                customerId = customerId,
                notes = notes,
                totalAmount = total
            )
        )

        quoteDao.saveItems(
            items.map { item ->
                item.copy(id = 0, quoteId = quoteId)
            }
        )

        return quoteId
    }

    suspend fun updateQuoteStatusAndNotes(
        quoteId: Long,
        status: String,
        notes: String
    ) {
        val quote = quoteDao.getQuoteById(quoteId) ?: return

        quoteDao.saveQuote(
            quote.copy(
                status = status,
                notes = notes
            )
        )
    }

    suspend fun addQuoteItem(item: QuoteItem): Long {
        val itemId = quoteDao.saveItem(item)
        recalculateQuoteTotal(item.quoteId)
        return itemId
    }

    suspend fun deleteQuoteItem(item: QuoteItem) {
        quoteDao.deleteItem(item)
        recalculateQuoteTotal(item.quoteId)
    }

    private suspend fun recalculateQuoteTotal(quoteId: Long) {
        val quote = quoteDao.getQuoteById(quoteId) ?: return
        val items = quoteDao.getQuoteItemsOnce(quoteId)
        val total = items.sumOf { it.lineTotal }

        quoteDao.saveQuote(
            quote.copy(totalAmount = total)
        )
    }

    private suspend fun nextQuoteNumber(): String {
        val next = quoteDao.countQuotes() + 1
        return "QF-${Year.now().value}-${next.toString().padStart(5, '0')}"
    }

    fun companySettings(): Flow<CompanySettings?> =
        companySettingsDao.getSettings()

    suspend fun saveCompanySettings(settings: CompanySettings) {
        companySettingsDao.save(settings)
    }
}