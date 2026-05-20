package com.smartnode.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Flexible identity record. Any card type can be modelled: military, employee,
 * visitor, contractor, etc. The fixed columns cover the bare minimum every card
 * shares (uid + holder name + primary identifier) and [jsonData] stores all the
 * card-specific extras (rank, blood type, dates, custodian, …) as a serialized
 * JSON object so the schema doesn't need to change every time a new card type
 * is added.
 *
 * Indices are placed on [cardType], [primaryIdentifier] and [holderName] so
 * lookups by card type or scan result stay O(log n) even with millions of rows.
 */
@Entity(
    tableName = "identities",
    indices = [
        Index(value = ["card_type"]),
        Index(value = ["primary_identifier"]),
        Index(value = ["holder_name"]),
    ],
)
data class IdentityEntity(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "card_type") val cardType: String,
    @ColumnInfo(name = "holder_name") val holderName: String,
    @ColumnInfo(name = "primary_identifier") val primaryIdentifier: String,
    @ColumnInfo(name = "json_data") val jsonData: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
