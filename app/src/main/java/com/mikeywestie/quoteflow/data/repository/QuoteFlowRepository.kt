package com.mikeywestie.quoteflow.data.repository

import com.mikeywestie.quoteflow.data.local.dao.CustomerDao
import com.mikeywestie.quoteflow.data.local.dao.ProductDao
import com.mikeywestie.quoteflow.data.local.dao.QuoteDao
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.Quote
import kotlinx.coroutines.flow.Flow
import java.time.Year

class QuoteFlowRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val quoteDao: QuoteDao
) {
    fun products(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    suspend fun saveProduct(product: Product) = productDao.save(product)
    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    fun customers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    suspend fun saveCustomer(customer: Customer) = customerDao.save(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    fun quotes(): Flow<List<Quote>> = quoteDao.getQuotes()
    suspend fun createDraftQuote(customerId: Long): Long {
        val number = nextQuoteNumber()
        return quoteDao.saveQuote(Quote(quoteNumber = number, customerId = customerId))
    }

    private suspend fun nextQuoteNumber(): String {
        val next = quoteDao.countQuotes() + 1
        return "QF-${Year.now().value}-${next.toString().padStart(5, '0')}"
    }
}
