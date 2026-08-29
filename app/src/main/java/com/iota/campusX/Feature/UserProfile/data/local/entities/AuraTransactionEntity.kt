package com.iota.campusX.Feature.UserProfile.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aura_transactions",
    indices = [
        Index(value = ["user_id", "type", "date_key"], unique = true)
    ]
)
data class AuraTransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val amount: Int,
    val type: String,
    @ColumnInfo(name = "reference_id") val referenceId: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "date_key") val dateKey: String // YYYY-MM-DD
)
