package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.*
import com.mikeywestie.quoteflow.data.local.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE customerName LIKE '%' || :query || '%' OR contactPerson LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY customerName")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)
}
