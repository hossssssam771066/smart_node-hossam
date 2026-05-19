package com.smartnode.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartnode.app.data.local.entity.SchemaProbeEntity

/**
 * Internal DAO used only to verify the encrypted database opens correctly
 * during Phase 1. Replaced by real DAOs in Phase 2.
 */
@Dao
internal interface SchemaProbeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SchemaProbeEntity)

    @Query("SELECT COUNT(*) FROM _schema_probe")
    suspend fun count(): Int
}
