package com.smartnode.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartnode.app.data.local.entity.TransactionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLogDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: TransactionLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(logs: List<TransactionLogEntity>): List<Long>

    @Query("SELECT * FROM transaction_logs WHERE log_id = :logId LIMIT 1")
    suspend fun findById(logId: Long): TransactionLogEntity?

    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs WHERE nfc_uid = :nfcUid ORDER BY timestamp DESC")
    fun observeByNfcUid(nfcUid: String): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs WHERE barcode_id = :barcodeId ORDER BY timestamp DESC")
    fun observeByBarcode(barcodeId: String): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs WHERE action_type = :actionType ORDER BY timestamp DESC")
    fun observeByAction(actionType: String): Flow<List<TransactionLogEntity>>

    @Query(
        """
        SELECT * FROM transaction_logs
        WHERE timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
        """,
    )
    fun observeBetween(from: Long, to: Long): Flow<List<TransactionLogEntity>>

    @Query("SELECT COUNT(*) FROM transaction_logs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM transaction_logs")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM transaction_logs")
    suspend fun clear()
}
