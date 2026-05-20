package com.smartnode.app.domain.repository

import com.smartnode.app.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {

    suspend fun save(asset: Asset): Result<Unit>
    suspend fun saveAll(assets: List<Asset>): Result<Unit>

    suspend fun findByBarcode(barcodeId: String): Result<Asset?>

    fun observeByBarcode(barcodeId: String): Flow<Asset?>
    fun observeAll(): Flow<List<Asset>>
    fun observeByCategory(category: String): Flow<List<Asset>>
    fun observeByStatus(status: String): Flow<List<Asset>>
    fun search(query: String): Flow<List<Asset>>

    suspend fun updateStatus(barcodeId: String, status: String): Result<Int>

    fun observeCount(): Flow<Int>

    suspend fun delete(barcodeId: String): Result<Int>
    suspend fun clear(): Result<Unit>
}
