package com.smartnode.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartnode.app.R
import com.smartnode.app.domain.repository.AssetRepository
import com.smartnode.app.domain.repository.IdentityRepository
import com.smartnode.app.domain.repository.TransactionLogRepository
import com.smartnode.app.presentation.theme.SmartNodeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var identityRepository: IdentityRepository
    @Inject lateinit var assetRepository: AssetRepository
    @Inject lateinit var transactionLogRepository: TransactionLogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartNodeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Phase2Screen(
                            identityRepository = identityRepository,
                            assetRepository = assetRepository,
                            transactionLogRepository = transactionLogRepository,
                        )
                    }
                }
            }
        }
    }
}

private enum class DbStatus { Loading, Ready, Error }

@Composable
private fun Phase2Screen(
    identityRepository: IdentityRepository,
    assetRepository: AssetRepository,
    transactionLogRepository: TransactionLogRepository,
) {
    var status by remember { mutableStateOf(DbStatus.Loading) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val identityCount by identityRepository.observeCount().collectAsState(initial = 0)
    val assetCount by assetRepository.observeCount().collectAsState(initial = 0)
    val logCount by transactionLogRepository.observeCount().collectAsState(initial = 0)

    LaunchedEffect(Unit) {
        status = try {
            // Each call below triggers SQLCipher → Room to actually open the
            // encrypted database; if any DAO is misconfigured this throws and
            // the UI flips into the error state.
            identityRepository.findByUid("__probe__")
            assetRepository.findByBarcode("__probe__")
            DbStatus.Ready
        } catch (t: Throwable) {
            errorMessage = t.message
            DbStatus.Error
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.phase_two_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.phase_two_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            DbStatusCard(status = status, errorMessage = errorMessage)

            Spacer(Modifier.height(16.dp))

            if (status == DbStatus.Ready) {
                CountsRow(identities = identityCount, assets = assetCount, logs = logCount)
                Spacer(Modifier.height(16.dp))
            }

            InfoChip(text = stringResource(R.string.security_aes_keystore))
            Spacer(Modifier.height(8.dp))
            InfoChip(text = stringResource(R.string.security_sqlcipher))
            Spacer(Modifier.height(8.dp))
            InfoChip(text = stringResource(R.string.flexible_schema_chip))
        }
    }
}

@Composable
private fun DbStatusCard(status: DbStatus, errorMessage: String?) {
    val (label, tint, icon) = when (status) {
        DbStatus.Loading -> Triple(stringResource(R.string.db_status_loading), MaterialTheme.colorScheme.primary, null)
        DbStatus.Ready -> Triple(stringResource(R.string.db_status_ready), Color(0xFF1F8F4E), Icons.Filled.CheckCircle)
        DbStatus.Error -> Triple(stringResource(R.string.db_status_error), Color(0xFFB3261E), Icons.Filled.Error)
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon == null) {
                CircularProgressIndicator(color = tint, strokeWidth = 3.dp)
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                color = tint,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            errorMessage?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun CountsRow(identities: Int, assets: Int, logs: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.count_identities),
            value = identities,
            icon = Icons.Filled.AccountBox,
        )
        CountCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.count_assets),
            value = assets,
            icon = Icons.Filled.Inventory2,
        )
        CountCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.count_logs),
            value = logs,
            icon = Icons.Filled.Receipt,
        )
    }
}

@Composable
private fun CountCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    icon: ImageVector,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}
