package com.smartnode.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartnode.app.data.local.dao.SchemaProbeDao
import com.smartnode.app.data.local.entity.SchemaProbeEntity

/**
 * Single Room database for the entire app — encrypted at rest with SQLCipher.
 *
 * Phase 1 ships with a temporary [SchemaProbeEntity] so Room can generate a
 * schema and so we can verify the cipher pipeline works end-to-end. Real
 * entities (Identity, Asset, TransactionLog) will be added in Phase 2.
 */
@Database(
    entities = [SchemaProbeEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SmartNodeDatabase : RoomDatabase() {

    internal abstract fun schemaProbeDao(): SchemaProbeDao

    companion object {
        const val NAME = "smart_node.db"
    }
}
