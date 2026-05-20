package com.smartnode.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smartnode.app.presentation.home.HomeScreen
import com.smartnode.app.presentation.identity.AddIdentityScreen
import com.smartnode.app.presentation.identity.IdentitiesScreen
import com.smartnode.app.presentation.logs.LogsScreen
import com.smartnode.app.presentation.scanner.ScannerScreen

/**
 * Single NavHost wiring every Phase 3 destination. Each screen pulls its own
 * Hilt-scoped ViewModel inside the composable so the graph itself stays free
 * of repository wiring.
 */
@Composable
fun SmartNodeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SmartNodeDestinations.HOME,
    ) {
        composable(SmartNodeDestinations.HOME) {
            HomeScreen(
                onOpenScanner = { navController.navigate(SmartNodeDestinations.SCANNER) },
                onOpenAddIdentity = { navController.navigate(SmartNodeDestinations.ADD_IDENTITY) },
                onOpenIdentities = { navController.navigate(SmartNodeDestinations.IDENTITIES) },
                onOpenLogs = { navController.navigate(SmartNodeDestinations.LOGS) },
            )
        }
        composable(SmartNodeDestinations.SCANNER) {
            ScannerScreen(onBack = { navController.popBackStack() })
        }
        composable(SmartNodeDestinations.ADD_IDENTITY) {
            AddIdentityScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(SmartNodeDestinations.IDENTITIES) {
            IdentitiesScreen(
                onBack = { navController.popBackStack() },
                onAddIdentity = { navController.navigate(SmartNodeDestinations.ADD_IDENTITY) },
            )
        }
        composable(SmartNodeDestinations.LOGS) {
            LogsScreen(onBack = { navController.popBackStack() })
        }
    }
}
