package com.smartnode.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartnode.app.data.local.converter.Converters
import com.smartnode.app.data.local.dao.AssetDao
import com.smartnode.app.data.local.dao.IdentityDao
import com.smartnode.app.data.local.dao.TransactionLogDao
import com.smartnode.app.data.local.entity.AssetEntity
import com.smartnode.app.data.local.entity.IdentityEntity
import com.smartnode.app.data.local.entity.TransactionLogEntity

/**
 * Encrypted Room database (SQLCipher). Three tables:
 *  - [IdentityEntity] : flexible per-card record, extras held in a JSON column.
 *  - [AssetEntity]    : physical / logical assets keyed by their barcode.
 *  - [TransactionLogEntity] : append-only audit log of every scan / action.
 */
@Database(
    entities = [
        IdentityEntity::class,
        AssetEntity::class,
        TransactionLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SmartNodeDatabase : RoomDatabase() {

    abstract fun identityDao(): IdentityDao
    abstract fun assetDao(): AssetDao
    abstract fun transactionLogDao(): TransactionLogDao

    companion object {
        const val NAME = "smart_node.db"
    }
}
