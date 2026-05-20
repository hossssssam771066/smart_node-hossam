package com.smartnode.app.presentation.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartnode.app.R
import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.usecase.AddIdentityUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIdentityScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: IdentityViewModel = hiltViewModel(),
) {
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val uidLabel = stringResource(R.string.field_uid)
    val holderLabel = stringResource(R.string.field_holder_name)
    val primaryLabel = stringResource(R.string.field_primary_identifier)
    val cardTypeLabel = stringResource(R.string.field_card_type)
    val duplicateUidMsg = stringResource(R.string.error_duplicate_uid)
    val uidBlankMsg = stringResource(R.string.error_uid_blank)
    val holderBlankMsg = stringResource(R.string.error_holder_blank)
    val primaryBlankMsg = stringResource(R.string.error_primary_blank)
    val storageErrorMsg = stringResource(R.string.error_storage)
    val savedMsg = stringResource(R.string.success_identity_saved)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is IdentityViewModel.UiEvent.IdentitySaved -> {
                    snackbarHostState.showSnackbar(savedMsg)
                    onSaved()
                }
                is IdentityViewModel.UiEvent.IdentityFailed -> {
                    val msg = when (val reason = event.reason) {
                        AddIdentityUseCase.Reason.UidBlank -> uidBlankMsg
                        AddIdentityUseCase.Reason.HolderNameBlank -> holderBlankMsg
                        AddIdentityUseCase.Reason.PrimaryIdentifierBlank -> primaryBlankMsg
                        AddIdentityUseCase.Reason.DuplicateUid -> duplicateUidMsg
                        is AddIdentityUseCase.Reason.Storage ->
                            reason.cause.localizedMessage?.let { "$storageErrorMsg: $it" } ?: storageErrorMsg
                    }
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_add_identity_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        AddIdentityForm(
            padding = innerPadding,
            isSubmitting = isSubmitting,
            uidLabel = uidLabel,
            holderLabel = holderLabel,
            primaryLabel = primaryLabel,
            cardTypeLabel = cardTypeLabel,
            onSubmit = { uid, type, holder, primary, extras ->
                focusManager.clearFocus(force = true)
                viewModel.addIdentity(uid, type, holder, primary, extras)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIdentityForm(
    padding: PaddingValues,
    isSubmitting: Boolean,
    uidLabel: String,
    holderLabel: String,
    primaryLabel: String,
    cardTypeLabel: String,
    onSubmit: (
        uid: String,
        cardType: CardType,
        holderName: String,
        primaryIdentifier: String,
        attributes: Map<String, String>,
    ) -> Unit,
) {
    var uid by rememberSaveable { mutableStateOf("") }
    var holderName by rememberSaveable { mutableStateOf("") }
    var primaryIdentifier by rememberSaveable { mutableStateOf("") }
    var cardType by rememberSaveable { mutableStateOf<CardType>(CardType.Military) }
    var menuExpanded by remember { mutableStateOf(false) }
    val dynamicRows = remember { mutableStateListOf<AttributeRow>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = uid,
            onValueChange = { uid = it },
            label = { Text(uidLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = !menuExpanded },
        ) {
            OutlinedTextField(
                value = cardType.key,
                onValueChange = {},
                readOnly = true,
                label = { Text(cardTypeLabel) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                CARD_TYPE_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.key) },
                        onClick = {
                            cardType = option
                            menuExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = holderName,
            onValueChange = { holderName = it },
            label = { Text(holderLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = primaryIdentifier,
            onValueChange = { primaryIdentifier = it },
            label = { Text(primaryLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.section_extra_attributes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.section_extra_attributes_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height((dynamicRows.size.coerceAtMost(4) * 96).dp + 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(dynamicRows, key = { it.id }) { row ->
                DynamicAttributeRow(
                    row = row,
                    onKeyChange = { row.key = it },
                    onValueChange = { row.value = it },
                    onRemove = { dynamicRows.remove(row) },
                )
            }
        }

        OutlinedButton(
            onClick = { dynamicRows.add(AttributeRow()) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_add_attribute))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val extras = dynamicRows
                    .filter { it.key.isNotBlank() }
                    .associate { it.key.trim() to it.value }
                onSubmit(uid, cardType, holderName, primaryIdentifier, extras)
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.action_save),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DynamicAttributeRow(
    row: AttributeRow,
    onKeyChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = row.key,
                onValueChange = onKeyChange,
                label = { Text(stringResource(R.string.field_attr_key)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = row.value,
                onValueChange = onValueChange,
                label = { Text(stringResource(R.string.field_attr_value)) },
                singleLine = true,
                modifier = Modifier.weight(1.4f),
            )
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private class AttributeRow(
    initialKey: String = "",
    initialValue: String = "",
) {
    val id: Long = NEXT_ID++
    var key: String = initialKey
    var value: String = initialValue

    private companion object {
        var NEXT_ID = 1L
    }
}

private val CARD_TYPE_OPTIONS: List<CardType> = listOf(
    CardType.Military,
    CardType.Employee,
    CardType.Visitor,
    CardType.Contractor,
)
