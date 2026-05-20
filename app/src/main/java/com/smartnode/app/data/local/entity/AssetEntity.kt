package com.smartnode.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Physical (or logical) asset that can be linked to an [IdentityEntity] via a
 * scan in [TransactionLogEntity]. The barcode value itself is the primary key
 * — assets are uniquely identified by what's printed on them.
 */
@Entity(
    tableName = "assets",
    indices = [
        Index(value = ["category"]),
        Index(value = ["status"]),
    ],
)
data class AssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "barcode_id") val barcodeId: String,
    @ColumnInfo(name = "asset_name") val assetName: String,
    val category: String,
    val status: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
