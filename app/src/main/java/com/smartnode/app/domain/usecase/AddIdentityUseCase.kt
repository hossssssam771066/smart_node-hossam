package com.smartnode.app.domain.usecase

import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.model.Identity
import com.smartnode.app.domain.repository.IdentityRepository
import javax.inject.Inject

/**
 * Persists a new identity record.
 *
 * Guards against duplicate UIDs by probing the repository first; the database
 * also enforces uniqueness at the PK level, but doing the check here lets the
 * UI surface a localized error before any I/O write hits SQLCipher.
 */
class AddIdentityUseCase @Inject constructor(
    private val identityRepository: IdentityRepository,
) {

    sealed class Result {
        data class Success(val identity: Identity) : Result()
        data class Failure(val reason: Reason) : Result()
    }

    sealed class Reason {
        data object UidBlank : Reason()
        data object HolderNameBlank : Reason()
        data object PrimaryIdentifierBlank : Reason()
        data object DuplicateUid : Reason()
        data class Storage(val cause: Throwable) : Reason()
    }

    suspend operator fun invoke(
        uid: String,
        cardType: CardType,
        holderName: String,
        primaryIdentifier: String,
        attributes: Map<String, Any?>,
    ): Result {
        val trimmedUid = uid.trim()
        val trimmedHolder = holderName.trim()
        val trimmedPrimary = primaryIdentifier.trim()

        if (trimmedUid.isEmpty()) return Result.Failure(Reason.UidBlank)
        if (trimmedHolder.isEmpty()) return Result.Failure(Reason.HolderNameBlank)
        if (trimmedPrimary.isEmpty()) return Result.Failure(Reason.PrimaryIdentifierBlank)

        val existing = identityRepository.findByUid(trimmedUid).getOrNull()
        if (existing != null) return Result.Failure(Reason.DuplicateUid)

        val identity = Identity(
            uid = trimmedUid,
            cardType = cardType,
            holderName = trimmedHolder,
            primaryIdentifier = trimmedPrimary,
            attributes = attributes.filterValues { it != null && it.toString().isNotBlank() },
            createdAt = System.currentTimeMillis(),
        )

        return identityRepository.save(identity).fold(
            onSuccess = { Result.Success(identity) },
            onFailure = { Result.Failure(Reason.Storage(it)) },
        )
    }
}
