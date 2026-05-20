package com.smartnode.app.data.mapper

import com.smartnode.app.data.local.entity.TransactionLogEntity
import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.TransactionLog

internal object TransactionLogMapper {

    fun TransactionLogEntity.toDomain(): TransactionLog = TransactionLog(
        logId = logId,
        nfcUid = nfcUid,
        barcodeId = barcodeId,
        actionType = ActionType.from(actionType),
        timestamp = timestamp,
    )

    fun TransactionLog.toEntity(): TransactionLogEntity = TransactionLogEntity(
        logId = logId,
        nfcUid = nfcUid,
        barcodeId = barcodeId,
        actionType = actionType.key,
        timestamp = timestamp,
    )
}
