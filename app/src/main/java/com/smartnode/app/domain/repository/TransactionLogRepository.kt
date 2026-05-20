package com.smartnode.app.domain.repository

import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.TransactionLog
import kotlinx.coroutines.flow.Flow

interface TransactionLogRepository {

    suspend fun append(log: TransactionLog): Result<Long>
    suspend fun appendAll(logs: List<TransactionLog>): Result<List<Long>>

    fun observeAll(): Flow<List<TransactionLog>>
    fun observeRecent(limit: Int): Flow<List<TransactionLog>>
    fun observeByNfcUid(nfcUid: String): Flow<List<TransactionLog>>
    fun observeByBarcode(barcodeId: String): Flow<List<TransactionLog>>
    fun observeByAction(actionType: ActionType): Flow<List<TransactionLog>>
    fun observeBetween(from: Long, to: Long): Flow<List<TransactionLog>>

    fun observeCount(): Flow<Int>

    suspend fun clear(): Result<Unit>
}
