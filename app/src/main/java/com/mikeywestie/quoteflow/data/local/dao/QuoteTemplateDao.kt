package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.*
import com.mikeywestie.quoteflow.data.local.entity.QuoteTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteTemplateDao {

    @Query("SELECT * FROM quote_templates ORDER BY templateName")
    fun getTemplates(): Flow<List<QuoteTemplate>>

    @Query("SELECT * FROM quote_templates ORDER BY templateName")
    suspend fun getTemplatesOnce(): List<QuoteTemplate>

    @Query("SELECT * FROM quote_templates WHERE id = :templateId LIMIT 1")
    suspend fun getTemplateById(templateId: Long): QuoteTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(template: QuoteTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(templates: List<QuoteTemplate>)

    @Delete
    suspend fun delete(template: QuoteTemplate)
}