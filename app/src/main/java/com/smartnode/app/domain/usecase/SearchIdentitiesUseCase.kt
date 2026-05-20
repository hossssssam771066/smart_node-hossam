package com.smartnode.app.domain.usecase

import com.smartnode.app.domain.model.Identity
import com.smartnode.app.domain.repository.IdentityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Reactive search over the identities table. Empty / blank queries fall back
 * to streaming every row so the UI can use a single flow instead of branching
 * on whether the search field is populated.
 */
class SearchIdentitiesUseCase @Inject constructor(
    private val identityRepository: IdentityRepository,
) {
    operator fun invoke(query: String): Flow<List<Identity>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            identityRepository.observeAll()
        } else {
            identityRepository.search(trimmed)
        }
    }
}
