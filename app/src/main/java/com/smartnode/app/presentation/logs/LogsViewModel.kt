package com.smartnode.app.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnode.app.domain.model.TransactionLog
import com.smartnode.app.domain.repository.TransactionLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Streams the most recent transaction log entries so the operator can audit
 * scans + handovers without ever leaving the offline app.
 */
@HiltViewModel
class LogsViewModel @Inject constructor(
    transactionLogRepository: TransactionLogRepository,
) : ViewModel() {

    val logs: StateFlow<List<TransactionLog>> = transactionLogRepository
        .observeRecent(LIMIT)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )

    private companion object {
        const val LIMIT = 200
    }
}
