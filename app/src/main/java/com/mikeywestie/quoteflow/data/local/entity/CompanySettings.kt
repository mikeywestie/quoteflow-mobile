package com.mikeywestie.quoteflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_settings")
data class CompanySettings(
    @PrimaryKey
    val id: Int = 1,

    val companyName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val vatNumber: String = "",
    val registrationNumber: String = ""
)