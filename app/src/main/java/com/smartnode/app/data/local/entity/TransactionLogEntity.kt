package com.smartnode.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only audit log. Every NFC tap, barcode scan, check-in / check-out,
 * handover, etc. produces one row. Both [nfcUid] and [barcodeId] are nullable
 * because a single event sometimes touches only one of them.
 */
@Entity(
    tableName = "transaction_logs",
    indices = [
        Index(value = ["nfc_uid"]),
        Index(value = ["barcode_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["action_type"]),
    ],
)
data class TransactionLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id") val logId: Long = 0L,
    @ColumnInfo(name = "nfc_uid") val nfcUid: String?,
    @ColumnInfo(name = "barcode_id") val barcodeId: String?,
    @ColumnInfo(name = "action_type") val actionType: String,
    val timestamp: Long,
)
