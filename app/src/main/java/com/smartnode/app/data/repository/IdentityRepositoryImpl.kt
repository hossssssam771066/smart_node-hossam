package com.smartnode.app.data.repository

import com.smartnode.app.data.local.dao.IdentityDao
import com.smartnode.app.data.mapper.IdentityMapper.toDomain
import com.smartnode.app.data.mapper.IdentityMapper.toEntity
import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.model.Identity
import com.smartnode.app.domain.repository.IdentityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class IdentityRepositoryImpl @Inject constructor(
    private val dao: IdentityDao,
) : IdentityRepository {

    override suspend fun save(identity: Identity): Result<Unit> = runCatching {
        dao.upsert(identity.toEntity())
    }

    override suspend fun saveAll(identities: List<Identity>): Result<Unit> = runCatching {
        dao.upsertAll(identities.map { it.toEntity() })
    }

    override suspend fun findByUid(uid: String): Result<Identity?> = runCatching {
        dao.findByUid(uid)?.toDomain()
    }

    override suspend fun findByPrimaryIdentifier(identifier: String): Result<Identity?> = runCatching {
        dao.findByPrimaryIdentifier(identifier)?.toDomain()
    }

    override fun observeByUid(uid: String): Flow<Identity?> =
        dao.observeByUid(uid).map { it?.toDomain() }

    override fun observeAll(): Flow<List<Identity>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeByCardType(cardType: CardType): Flow<List<Identity>> =
        dao.observeByCardType(cardType.key).map { rows -> rows.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Identity>> =
        dao.search(query).map { rows -> rows.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun delete(uid: String): Result<Int> = runCatching {
        dao.deleteByUid(uid)
    }

    override suspend fun clear(): Result<Unit> = runCatching {
        dao.clear()
    }
}
