package com.smartnode.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.smartnode.app.presentation.navigation.SmartNodeNavGraph
import com.smartnode.app.presentation.theme.SmartNodeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * App entry point. Hosts the Compose NavHost which fans out into the Phase 3
 * screens (home, scanner, identities, logs, add identity). All UI is wrapped
 * in `LayoutDirection.Rtl` so the Arabic content lays out correctly regardless
 * of device locale.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartNodeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        SmartNodeNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
