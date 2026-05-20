package com.smartnode.app.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnode.app.domain.model.ActionType
import com.smartnode.app.domain.model.Asset
import com.smartnode.app.domain.model.TransactionLog
import com.smartnode.app.domain.usecase.LogTransactionUseCase
import com.smartnode.app.domain.usecase.ScanBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State holder for the offline barcode / NFC scanner screen.
 *
 * The Compose screen feeds raw payloads in through [onBarcodeDetected] /
 * [onNfcTagDetected] and observes [state] for UI updates. Side effects
 * (snackbar messages) flow through [events] as one-shot signals so they
 * don't replay on configuration change.
 *
 * All persistence work goes through the use-cases, which write to the
 * encrypted Room database — no network calls happen here.
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val logTransactionUseCase: LogTransactionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScannerEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    /** Latest raw payload we processed; used to debounce repeated frames. */
    private var lastPayload: String? = null
    private var lastPayloadAt: Long = 0L

    fun onBarcodeDetected(rawPayload: String) {
        val now = System.currentTimeMillis()
        if (rawPayload == lastPayload && now - lastPayloadAt < REPEAT_DEBOUNCE_MS) return
        lastPayload = rawPayload
        lastPayloadAt = now

        _state.value = ScanState.Processing(rawPayload)

        viewModelScope.launch {
            val newState: ScanState = when (val result = scanBarcodeUseCase(rawPayload = rawPayload)) {
                is ScanBarcodeUseCase.Result.Match -> ScanState.Matched(result.asset, result.log)
                is ScanBarcodeUseCase.Result.Registered -> ScanState.Registered(result.asset, result.log)
                is ScanBarcodeUseCase.Result.EmptyPayload -> {
                    _events.emit(ScannerEvent.EmptyPayload)
                    ScanState.Idle
                }
                is ScanBarcodeUseCase.Result.Failure -> {
                    _events.emit(ScannerEvent.Failure(result.cause))
                    ScanState.Idle
                }
            }
            _state.value = newState
        }
    }

    fun onNfcTagDetected(uid: String) {
        viewModelScope.launch {
            val log = logTransactionUseCase(
                actionType = ActionType.IdentityScan,
                nfcUid = uid,
            )
            log.fold(
                onSuccess = { saved ->
                    _state.value = ScanState.NfcRead(uid, saved)
                },
                onFailure = { _events.emit(ScannerEvent.Failure(it)) },
            )
        }
    }

    fun reset() {
        lastPayload = null
        lastPayloadAt = 0L
        _state.value = ScanState.Idle
    }

    sealed class ScanState {
        data object Idle : ScanState()
        data class Processing(val rawPayload: String) : ScanState()
        data class Matched(val asset: Asset, val log: TransactionLog) : ScanState()
        data class Registered(val asset: Asset, val log: TransactionLog) : ScanState()
        data class NfcRead(val uid: String, val log: TransactionLog) : ScanState()
    }

    sealed class ScannerEvent {
        data object EmptyPayload : ScannerEvent()
        data class Failure(val cause: Throwable) : ScannerEvent()
    }

    private companion object {
        const val REPEAT_DEBOUNCE_MS = 2_000L
    }
}
