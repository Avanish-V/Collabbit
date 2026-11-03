package com.iota.campusX.Feature.UserProfile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iota.campusX.Feature.UserProfile.data.local.entities.AuraTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: AuraTransactionEntity)

    @Query("SELECT * FROM aura_transactions WHERE user_id = :userId AND type = :type AND date_key = :dateKey LIMIT 1")
    suspend fun getTransactionByTypeAndDate(userId: String, type: String, dateKey: String): AuraTransactionEntity?

    @Query("SELECT * FROM aura_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeTransactions(userId: String): Flow<List<AuraTransactionEntity>>

    @Query("DELETE FROM aura_transactions WHERE user_id = :userId")
    suspend fun clearTransactions(userId: String)
}
