package com.smartnode.app.data.repository

import com.smartnode.app.data.local.dao.AssetDao
import com.smartnode.app.data.mapper.AssetMapper.toDomain
import com.smartnode.app.data.mapper.AssetMapper.toEntity
import com.smartnode.app.domain.model.Asset
import com.smartnode.app.domain.repository.AssetRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AssetRepositoryImpl @Inject constructor(
    private val dao: AssetDao,
) : AssetRepository {

    override suspend fun save(asset: Asset): Result<Unit> = runCatching {
        dao.upsert(asset.toEntity())
    }

    override suspend fun saveAll(assets: List<Asset>): Result<Unit> = runCatching {
        dao.upsertAll(assets.map { it.toEntity() })
    }

    override suspend fun findByBarcode(barcodeId: String): Result<Asset?> = runCatching {
        dao.findByBarcode(barcodeId)?.toDomain()
    }

    override fun observeByBarcode(barcodeId: String): Flow<Asset?> =
        dao.observeByBarcode(barcodeId).map { it?.toDomain() }

    override fun observeAll(): Flow<List<Asset>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeByCategory(category: String): Flow<List<Asset>> =
        dao.observeByCategory(category).map { rows -> rows.map { it.toDomain() } }

    override fun observeByStatus(status: String): Flow<List<Asset>> =
        dao.observeByStatus(status).map { rows -> rows.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Asset>> =
        dao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun updateStatus(barcodeId: String, status: String): Result<Int> = runCatching {
        dao.updateStatus(barcodeId, status, System.currentTimeMillis())
    }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun delete(barcodeId: String): Result<Int> = runCatching {
        dao.deleteByBarcode(barcodeId)
    }

    override suspend fun clear(): Result<Unit> = runCatching {
        dao.clear()
    }
}
