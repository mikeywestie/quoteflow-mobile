package com.mikeywestie.quoteflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mikeywestie.quoteflow.data.local.entity.CompanySettings
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanySettingsDao {

    @Query("SELECT * FROM company_settings WHERE id = 1")
    fun getSettings(): Flow<CompanySettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: CompanySettings)
}