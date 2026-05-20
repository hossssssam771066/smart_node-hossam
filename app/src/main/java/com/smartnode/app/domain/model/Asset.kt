package com.smartnode.app.domain.model

/**
 * Physical or logical asset (laptop, weapon, vehicle, badge, …) that is
 * identified by the value printed on its barcode. Status / category are kept
 * as free-form strings so the workflow can evolve without schema migrations.
 */
data class Asset(
    val barcodeId: String,
    val assetName: String,
    val category: String,
    val status: String,
    val updatedAt: Long,
)
