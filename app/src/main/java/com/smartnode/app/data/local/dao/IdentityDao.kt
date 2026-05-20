package com.smartnode.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.smartnode.app.data.local.entity.IdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentityDao {

    @Upsert
    suspend fun upsert(identity: IdentityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(identities: List<IdentityEntity>)

    @Query("SELECT * FROM identities WHERE uid = :uid LIMIT 1")
    suspend fun findByUid(uid: String): IdentityEntity?

    @Query("SELECT * FROM identities WHERE primary_identifier = :identifier LIMIT 1")
    suspend fun findByPrimaryIdentifier(identifier: String): IdentityEntity?

    @Query("SELECT * FROM identities WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<IdentityEntity?>

    @Query("SELECT * FROM identities ORDER BY created_at DESC")
    fun observeAll(): Flow<List<IdentityEntity>>

    @Query("SELECT * FROM identities WHERE card_type = :cardType ORDER BY created_at DESC")
    fun observeByCardType(cardType: String): Flow<List<IdentityEntity>>

    @Query(
        """
        SELECT * FROM identities
        WHERE holder_name LIKE '%' || :query || '%'
           OR primary_identifier LIKE '%' || :query || '%'
        ORDER BY created_at DESC
        """,
    )
    fun search(query: String): Flow<List<IdentityEntity>>

    @Query("SELECT COUNT(*) FROM identities")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM identities")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM identities WHERE uid = :uid")
    suspend fun deleteByUid(uid: String): Int

    @Query("DELETE FROM identities")
    suspend fun clear()
}
