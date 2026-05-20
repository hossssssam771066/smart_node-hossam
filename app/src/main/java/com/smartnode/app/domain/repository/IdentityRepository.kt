package com.smartnode.app.domain.repository

import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.model.Identity
import kotlinx.coroutines.flow.Flow

/**
 * Contract the presentation / use-case layer talks to. The data layer provides
 * an implementation that maps JSON attributes onto / off of the underlying
 * Room table.
 */
interface IdentityRepository {

    suspend fun save(identity: Identity): Result<Unit>
    suspend fun saveAll(identities: List<Identity>): Result<Unit>

    suspend fun findByUid(uid: String): Result<Identity?>
    suspend fun findByPrimaryIdentifier(identifier: String): Result<Identity?>

    fun observeByUid(uid: String): Flow<Identity?>
    fun observeAll(): Flow<List<Identity>>
    fun observeByCardType(cardType: CardType): Flow<List<Identity>>
    fun search(query: String): Flow<List<Identity>>

    fun observeCount(): Flow<Int>

    suspend fun delete(uid: String): Result<Int>
    suspend fun clear(): Result<Unit>
}
