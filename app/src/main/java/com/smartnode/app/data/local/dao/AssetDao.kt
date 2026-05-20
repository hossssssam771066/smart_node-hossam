package com.smartnode.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.smartnode.app.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Upsert
    suspend fun upsert(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("SELECT * FROM assets WHERE barcode_id = :barcodeId LIMIT 1")
    suspend fun findByBarcode(barcodeId: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE barcode_id = :barcodeId LIMIT 1")
    fun observeByBarcode(barcodeId: String): Flow<AssetEntity?>

    @Query("SELECT * FROM assets ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY updated_at DESC")
    fun observeByCategory(category: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE status = :status ORDER BY updated_at DESC")
    fun observeByStatus(status: String): Flow<List<AssetEntity>>

    @Query(
        """
        SELECT * FROM assets
        WHERE asset_name LIKE '%' || :query || '%'
           OR barcode_id LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
        """,
    )
    fun search(query: String): Flow<List<AssetEntity>>

    @Query("UPDATE assets SET status = :status, updated_at = :updatedAt WHERE barcode_id = :barcodeId")
    suspend fun updateStatus(barcodeId: String, status: String, updatedAt: Long): Int

    @Query("SELECT COUNT(*) FROM assets")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM assets")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM assets WHERE barcode_id = :barcodeId")
    suspend fun deleteByBarcode(barcodeId: String): Int

    @Query("DELETE FROM assets")
    suspend fun clear()
}
