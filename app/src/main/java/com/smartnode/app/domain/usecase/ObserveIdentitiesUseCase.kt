package com.smartnode.app.domain.usecase

import com.smartnode.app.domain.model.Identity
import com.smartnode.app.domain.repository.IdentityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams every identity in the database. The list flows back through Room's
 * Flow integration so any insert / update / delete automatically refreshes
 * downstream consumers.
 */
class ObserveIdentitiesUseCase @Inject constructor(
    private val identityRepository: IdentityRepository,
) {
    operator fun invoke(): Flow<List<Identity>> = identityRepository.observeAll()
}
