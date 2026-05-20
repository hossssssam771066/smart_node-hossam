package com.smartnode.app.data.mapper

import com.smartnode.app.data.local.entity.AssetEntity
import com.smartnode.app.domain.model.Asset

internal object AssetMapper {

    fun AssetEntity.toDomain(): Asset = Asset(
        barcodeId = barcodeId,
        assetName = assetName,
        category = category,
        status = status,
        updatedAt = updatedAt,
    )

    fun Asset.toEntity(): AssetEntity = AssetEntity(
        barcodeId = barcodeId,
        assetName = assetName,
        category = category,
        status = status,
        updatedAt = updatedAt,
    )
}
