package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.*
import com.mikeywestie.quoteflow.data.local.entity.TemplateItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateItemDao {

    @Query("""
        SELECT *
        FROM template_items
        WHERE templateId = :templateId
        ORDER BY id
    """)
    fun getTemplateItems(
        templateId: Long
    ): Flow<List<TemplateItem>>

    @Query("""
        SELECT *
        FROM template_items
        WHERE templateId = :templateId
        ORDER BY id
    """)
    suspend fun getTemplateItemsOnce(
        templateId: Long
    ): List<TemplateItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(
        item: TemplateItem
    ): Long

    @Delete
    suspend fun delete(
        item: TemplateItem
    )
}