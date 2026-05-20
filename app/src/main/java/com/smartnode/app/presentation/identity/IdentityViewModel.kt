package com.smartnode.app.presentation.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.model.Identity
import com.smartnode.app.domain.usecase.AddIdentityUseCase
import com.smartnode.app.domain.usecase.SearchIdentitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the "browse / search / add" identity screens.
 *
 * - `identities` streams matches against [query] reactively from the DB
 * - `events` carries one-shot UX signals (success snackbar, error toast)
 *   that the UI consumes without re-emitting them on configuration change.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class IdentityViewModel @Inject constructor(
    private val addIdentityUseCase: AddIdentityUseCase,
    searchIdentitiesUseCase: SearchIdentitiesUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val identities: StateFlow<List<Identity>> = _query
        .debounce(150L)
        .distinctUntilChanged()
        .flatMapLatest { searchIdentitiesUseCase(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    fun onQueryChanged(value: String) {
        _query.value = value
    }

    fun addIdentity(
        uid: String,
        cardType: CardType,
        holderName: String,
        primaryIdentifier: String,
        attributes: Map<String, String>,
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        viewModelScope.launch {
            val result = addIdentityUseCase(
                uid = uid,
                cardType = cardType,
                holderName = holderName,
                primaryIdentifier = primaryIdentifier,
                attributes = attributes,
            )
            _isSubmitting.value = false
            _events.emit(
                when (result) {
                    is AddIdentityUseCase.Result.Success -> UiEvent.IdentitySaved(result.identity)
                    is AddIdentityUseCase.Result.Failure -> UiEvent.IdentityFailed(result.reason)
                }
            )
        }
    }

    sealed class UiEvent {
        data class IdentitySaved(val identity: Identity) : UiEvent()
        data class IdentityFailed(val reason: AddIdentityUseCase.Reason) : UiEvent()
    }
}
