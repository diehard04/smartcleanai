package com.diehard.smartcleanai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.ui.dashboard.DashboardScreen
import com.diehard.smartcleanai.ui.navigation.Screen
import com.diehard.smartcleanai.ui.onboarding.OnboardingScreen
import com.diehard.smartcleanai.ui.review.ReviewScreen
import com.diehard.smartcleanai.ui.scanning.ScanningScreen
import com.diehard.smartcleanai.ui.settings.IgnoredMediaScreen
import com.diehard.smartcleanai.ui.settings.SettingsScreen
import com.diehard.smartcleanai.ui.summary.SummaryScreen
import com.diehard.smartcleanai.ui.theme.BgDark
import com.diehard.smartcleanai.ui.theme.SmartCleanAITheme
import com.diehard.smartcleanai.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasStoragePermission = checkStoragePermission()
        viewModel.onPermissionsResult(hasStoragePermission)

        setContent {
            SmartCleanAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        startDestination = if (hasStoragePermission) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Onboarding.route
                        }
                    )
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    startDestination: String
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Scoped Storage Trash / Delete Request Launcher (Android 11+)
    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val freedBytes = uiState.pendingDeleteBytes
            val count = uiState.pendingDeleteIds.size
            viewModel.onDeleteConfirmed(success = true)
            navController.navigate(Screen.Summary.createRoute(freedBytes, count)) {
                popUpTo(Screen.Dashboard.route) { inclusive = false }
            }
        } else {
            viewModel.onDeleteConfirmed(success = false)
        }
    }

    // Launch Android 11+ system trash confirmation dialog when pendingDeleteIntent is present
    LaunchedEffect(uiState.pendingDeleteIntent) {
        val pendingIntent = uiState.pendingDeleteIntent
        if (pendingIntent != null) {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            trashLauncher.launch(intentSenderRequest)
            viewModel.clearPendingDeleteIntent()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Screen 1: Splash & Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onPermissionsGranted = {
                    viewModel.onPermissionsResult(true)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 2: Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                uiState = uiState,
                onStartScan = {
                    viewModel.startScan(isIncremental = true)
                    navController.navigate(Screen.Scanning.route)
                },
                onRefreshStorage = {
                    viewModel.refreshStorageStats()
                },
                onNavigateToCategory = { category ->
                    navController.navigate(Screen.Review.createRoute(category.name))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Screen 3: Scanning Overlay
        composable(Screen.Scanning.route) {
            ScanningScreen(
                scanProgress = uiState.scanProgress,
                isScanning = uiState.isScanning,
                hasCompletedScan = uiState.hasCompletedScan,
                onStopScan = {
                    viewModel.stopScan()
                    navController.popBackStack()
                },
                onScanFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 4: Category Review & Compare
        composable(
            route = Screen.Review.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryStr = backStackEntry.arguments?.getString("category") ?: MediaCategory.DUPLICATE.name
            val category = try {
                MediaCategory.valueOf(categoryStr)
            } catch (_: Exception) {
                MediaCategory.DUPLICATE
            }

            ReviewScreen(
                initialCategory = category,
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onToggleSelect = { id -> viewModel.toggleItemSelection(id) },
                onSelectAll = { cat, select -> viewModel.selectAll(cat, select) },
                onSetBestShot = { groupId, newBestId -> viewModel.setBestShot(groupId, newBestId) },
                onOpenCompare = { item -> viewModel.openCompareDialog(item) },
                onCloseCompare = { viewModel.closeCompareDialog() },
                onToggleIgnore = { item -> viewModel.toggleIgnoreItem(item) },
                onCleanNow = { cat -> viewModel.initiateClean(cat, isPermanent = false) }
            )
        }

        // Screen 5: Summary
        composable(
            route = Screen.Summary.route,
            arguments = listOf(
                navArgument("freedBytes") { type = NavType.LongType },
                navArgument("itemCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val freedBytes = backStackEntry.arguments?.getLong("freedBytes") ?: 0L
            val itemCount = backStackEntry.arguments?.getInt("itemCount") ?: 0

            SummaryScreen(
                freedBytes = freedBytes,
                itemCount = itemCount,
                lifetimeSavedFormatted = uiState.formattedLifetimeSaved,
                onDone = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 6: Settings & Pro Upgrade
        composable(Screen.Settings.route) {
            SettingsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onUpdateBlurSensitivity = { threshold -> viewModel.updateBlurSensitivity(threshold) },
                onToggleAutoScan = { enabled -> viewModel.toggleAutoScan(enabled) },
                onNavigateToIgnored = { navController.navigate(Screen.IgnoredMedia.route) }
            )
        }

        // Screen 7: Protected / Ignored Media
        composable(Screen.IgnoredMedia.route) {
            IgnoredMediaScreen(
                ignoredItems = uiState.ignoredItems,
                onBack = { navController.popBackStack() },
                onUnprotectItem = { item -> viewModel.toggleIgnoreItem(item) }
            )
        }
    }
}
