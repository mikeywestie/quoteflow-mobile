package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.*
import com.mikeywestie.quoteflow.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    suspend fun getAllProductsOnce(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): Product?
}