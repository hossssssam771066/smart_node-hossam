package com.smartnode.app.domain.model

/**
 * Single entry in the immutable audit log. Either [nfcUid] or [barcodeId] (or
 * both) is expected to be non-null — a row with neither is meaningless.
 */
data class TransactionLog(
    val logId: Long,
    val nfcUid: String?,
    val barcodeId: String?,
    val actionType: ActionType,
    val timestamp: Long,
)

/**
 * Open set of action types. Common operations have first-class entries; any
 * other label round-trips through [Custom] so partner-specific workflows can
 * persist without schema changes.
 */
sealed class ActionType(val key: String) {
    data object IdentityScan : ActionType("IdentityScan")
    data object AssetScan : ActionType("AssetScan")
    data object Handover : ActionType("Handover")
    data object Return : ActionType("Return")
    data object CheckIn : ActionType("CheckIn")
    data object CheckOut : ActionType("CheckOut")
    data class Custom(val raw: String) : ActionType(raw)

    companion object {
        fun from(raw: String?): ActionType = when (raw?.trim()) {
            null, "" -> Custom("Unknown")
            IdentityScan.key -> IdentityScan
            AssetScan.key -> AssetScan
            Handover.key -> Handover
            Return.key -> Return
            CheckIn.key -> CheckIn
            CheckOut.key -> CheckOut
            else -> Custom(raw)
        }
    }
}
