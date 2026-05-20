package com.smartnode.app.domain.usecase

import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.TransactionLog
import com.smartnode.app.domain.repository.TransactionLogRepository
import javax.inject.Inject

/**
 * Appends an audit entry to the encrypted transaction log.
 *
 * Used by the scanner flow and any other action that needs a record of "this
 * happened at this time". The log is append-only — the use case never updates
 * or deletes existing rows.
 */
class LogTransactionUseCase @Inject constructor(
    private val transactionLogRepository: TransactionLogRepository,
) {

    suspend operator fun invoke(
        actionType: ActionType,
        nfcUid: String? = null,
        barcodeId: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ): Result<TransactionLog> {
        val entry = TransactionLog(
            logId = 0L,
            nfcUid = nfcUid?.trim()?.takeIf { it.isNotEmpty() },
            barcodeId = barcodeId?.trim()?.takeIf { it.isNotEmpty() },
            actionType = actionType,
            timestamp = timestamp,
        )
        // The repository returns the freshly assigned row id; merge it back in
        // so the caller receives a fully populated [TransactionLog].
        return transactionLogRepository.append(entry).map { newId -> entry.copy(logId = newId) }
    }
}
