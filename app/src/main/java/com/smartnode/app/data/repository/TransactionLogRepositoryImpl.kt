package com.smartnode.app.data.repository

import com.smartnode.app.data.local.dao.TransactionLogDao
import com.smartnode.app.data.mapper.TransactionLogMapper.toDomain
import com.smartnode.app.data.mapper.TransactionLogMapper.toEntity
import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.TransactionLog
import com.smartnode.app.domain.repository.TransactionLogRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TransactionLogRepositoryImpl @Inject constructor(
    private val dao: TransactionLogDao,
) : TransactionLogRepository {

    override suspend fun append(log: TransactionLog): Result<Long> = runCatching {
        dao.insert(log.toEntity())
    }

    override suspend fun appendAll(logs: List<TransactionLog>): Result<List<Long>> = runCatching {
        dao.insertAll(logs.map { it.toEntity() })
    }

    override fun observeAll(): Flow<List<TransactionLog>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<TransactionLog>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }

    override fun observeByNfcUid(nfcUid: String): Flow<List<TransactionLog>> =
        dao.observeByNfcUid(nfcUid).map { rows -> rows.map { it.toDomain() } }

    override fun observeByBarcode(barcodeId: String): Flow<List<TransactionLog>> =
        dao.observeByBarcode(barcodeId).map { rows -> rows.map { it.toDomain() } }

    override fun observeByAction(actionType: ActionType): Flow<List<TransactionLog>> =
        dao.observeByAction(actionType.key).map { rows -> rows.map { it.toDomain() } }

    override fun observeBetween(from: Long, to: Long): Flow<List<TransactionLog>> =
        dao.observeBetween(from, to).map { rows -> rows.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun clear(): Result<Unit> = runCatching {
        dao.clear()
    }
}
