package com.smartnode.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Temporary bootstrap entity used only to make Room generate a database
 * schema during Phase 1.
 *
 * Will be removed (or replaced by real entities) the moment the user gives
 * the green light for Phase 2: IdentityEntity, AssetEntity, TransactionLogEntity.
 */
@Entity(tableName = "_schema_probe")
data class SchemaProbeEntity(
    @PrimaryKey val id: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
)
