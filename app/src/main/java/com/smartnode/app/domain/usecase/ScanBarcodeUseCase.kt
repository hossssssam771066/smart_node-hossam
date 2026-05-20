package com.smartnode.app.domain.usecase

import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.Asset
import com.smartnode.app.domain.model.TransactionLog
import com.smartnode.app.domain.repository.AssetRepository
import com.smartnode.app.domain.repository.TransactionLogRepository
import javax.inject.Inject

/**
 * Handles the "user just scanned a barcode" flow.
 *
 * Look up the corresponding asset (if any), upsert a placeholder when it's a
 * brand new code we haven't seen before, and write an [ActionType.AssetScan]
 * row to the transaction log. The repository writes are wrapped so the UI
 * receives a structured result instead of raw throwables.
 */
class ScanBarcodeUseCase @Inject constructor(
    private val assetRepository: AssetRepository,
    private val transactionLogRepository: TransactionLogRepository,
) {

    sealed class Result {
        /** Existing asset matched the scanned barcode. */
        data class Match(val asset: Asset, val log: TransactionLog) : Result()

        /** Scanned barcode wasn't in the DB; we registered it as a placeholder. */
        data class Registered(val asset: Asset, val log: TransactionLog) : Result()

        /** Scanned payload was empty / whitespace only. */
        data object EmptyPayload : Result()

        /** Storage / DB write failed. */
        data class Failure(val cause: Throwable) : Result()
    }

    suspend operator fun invoke(
        rawPayload: String,
        registerIfMissing: Boolean = true,
    ): Result {
        val barcodeId = rawPayload.trim()
        if (barcodeId.isEmpty()) return Result.EmptyPayload

        val now = System.currentTimeMillis()

        val existing = assetRepository.findByBarcode(barcodeId).getOrNull()
        val (asset, isNew) = if (existing != null) {
            existing to false
        } else if (registerIfMissing) {
            val placeholder = Asset(
                barcodeId = barcodeId,
                assetName = barcodeId,
                category = UNKNOWN_CATEGORY,
                status = STATUS_REGISTERED,
                updatedAt = now,
            )
            val saved = assetRepository.save(placeholder)
            if (saved.isFailure) return Result.Failure(saved.exceptionOrNull() ?: IllegalStateException("save failed"))
            placeholder to true
        } else {
            // No existing record and the caller asked us not to create one.
            // Still log the scan so the operator has an audit trail.
            val pendingLog = TransactionLog(
                logId = 0L,
                nfcUid = null,
                barcodeId = barcodeId,
                actionType = ActionType.AssetScan,
                timestamp = now,
            )
            val logResult = transactionLogRepository.append(pendingLog)
            return logResult.fold(
                onSuccess = { newLogId ->
                    Result.Registered(
                        asset = Asset(
                            barcodeId = barcodeId,
                            assetName = barcodeId,
                            category = UNKNOWN_CATEGORY,
                            status = STATUS_UNKNOWN,
                            updatedAt = now,
                        ),
                        log = pendingLog.copy(logId = newLogId),
                    )
                },
                onFailure = { Result.Failure(it) },
            )
        }

        val log = TransactionLog(
            logId = 0L,
            nfcUid = null,
            barcodeId = asset.barcodeId,
            actionType = ActionType.AssetScan,
            timestamp = now,
        )
        return transactionLogRepository.append(log).fold(
            onSuccess = { newLogId ->
                val savedLog = log.copy(logId = newLogId)
                if (isNew) Result.Registered(asset, savedLog) else Result.Match(asset, savedLog)
            },
            onFailure = { Result.Failure(it) },
        )
    }

    private companion object {
        const val UNKNOWN_CATEGORY = "Uncategorized"
        const val STATUS_REGISTERED = "Registered"
        const val STATUS_UNKNOWN = "Unknown"
    }
}
