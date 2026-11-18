package com.diehard.smartcleanai.ui.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.ui.theme.AmberWarning
import com.diehard.smartcleanai.ui.theme.BgDark
import com.diehard.smartcleanai.ui.theme.CardDark
import com.diehard.smartcleanai.ui.theme.CoralRed
import com.diehard.smartcleanai.ui.theme.ElectricCyan
import com.diehard.smartcleanai.ui.theme.EmeraldGreen
import com.diehard.smartcleanai.ui.theme.OutlineDark
import com.diehard.smartcleanai.ui.theme.PrimaryBlue
import com.diehard.smartcleanai.ui.theme.PurpleAccent
import com.diehard.smartcleanai.ui.theme.SurfaceDark
import com.diehard.smartcleanai.ui.theme.WhatsAppGreen
import com.diehard.smartcleanai.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: MainUiState,
    onStartScan: () -> Unit,
    onRefreshStorage: () -> Unit,
    onNavigateToCategory: (MediaCategory) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, ElectricCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SmartClean AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshStorage) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Card: Storage Gauge & Smart Scan CTA
            item(span = { GridItemSpan(2) }) {
                StorageGaugeCard(
                    uiState = uiState,
                    onStartScan = onStartScan
                )
            }

            // 2. Section Header: Category Cards
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cleaning Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (uiState.totalReclaimableBytes > 0) {
                        Text(
                            text = "Reclaimable: ${uiState.formattedTotalReclaimable}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberWarning
                        )
                    }
                }
            }

            // 3. Middle Grid: 2x2 Category Cards
            // Card 1: Similar & Duplicates
            item {
                val catResult = uiState.scanResults[MediaCategory.DUPLICATE]
                CategoryCard(
                    title = "Similar & Duplicates",
                    count = catResult?.itemCount ?: 0,
                    sizeFormatted = catResult?.formattedTotalBytes ?: "0 MB",
                    icon = Icons.Default.ContentCopy,
                    accentColor = ElectricCyan,
                    onClick = { onNavigateToCategory(MediaCategory.DUPLICATE) }
                )
            }

            // Card 2: Blurry & Dark Photos
            item {
                val catResult = uiState.scanResults[MediaCategory.BLURRY]
                CategoryCard(
                    title = "Blurry & Dark Photos",
                    count = catResult?.itemCount ?: 0,
                    sizeFormatted = catResult?.formattedTotalBytes ?: "0 MB",
                    icon = Icons.Default.MotionPhotosOff,
                    accentColor = CoralRed,
                    onClick = { onNavigateToCategory(MediaCategory.BLURRY) }
                )
            }

            // Card 3: WhatsApp Junk & Memes
            item {
                val catResult = uiState.scanResults[MediaCategory.WHATSAPP_JUNK]
                CategoryCard(
                    title = "WhatsApp Junk",
                    count = catResult?.itemCount ?: 0,
                    sizeFormatted = catResult?.formattedTotalBytes ?: "0 MB",
                    icon = Icons.Default.FolderShared,
                    accentColor = WhatsAppGreen,
                    onClick = { onNavigateToCategory(MediaCategory.WHATSAPP_JUNK) }
                )
            }

            // Card 4: Old Screenshots & Videos (combines screenshots & large videos)
            item {
                val screenshotResult = uiState.scanResults[MediaCategory.SCREENSHOT]
                val videoResult = uiState.scanResults[MediaCategory.LARGE_VIDEO]
                val combinedCount = (screenshotResult?.itemCount ?: 0) + (videoResult?.itemCount ?: 0)
                val combinedBytes = (screenshotResult?.totalBytes ?: 0L) + (videoResult?.totalBytes ?: 0L)
                val mb = combinedBytes / (1024.0 * 1024.0)
                val gb = mb / 1024.0
                val formattedSize = if (gb >= 1.0) String.format("%.2f GB", gb) else String.format("%.0f MB", mb)

                CategoryCard(
                    title = "Screenshots & Videos",
                    count = combinedCount,
                    sizeFormatted = formattedSize,
                    icon = Icons.Default.VideoLibrary,
                    accentColor = PurpleAccent,
                    onClick = { onNavigateToCategory(MediaCategory.SCREENSHOT) }
                )
            }

            // 4. Lifetime Space Saved Badge
            item(span = { GridItemSpan(2) }) {
                LifetimeSavedCard(lifetimeSavedFormatted = uiState.formattedLifetimeSaved)
            }

            // 5. Bottom Section: Adaptive Banner / Privacy Note
            item(span = { GridItemSpan(2) }) {
                AdMobBannerPlaceholder()
            }
        }
    }
}

@Composable
fun StorageGaugeCard(
    uiState: MainUiState,
    onStartScan: () -> Unit
) {
    val stats = uiState.storageStats
    val percentage = stats.usedPercentage.coerceIn(0f, 100f)

    // Animated arc percentage
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage / 100f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Device Storage",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Storage Gauge
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                val strokeWidth = 14.dp
                val progressFraction = animatedProgress.value

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )

                    // Active progress arc with gradient feel
                    val arcColor = when {
                        percentage > 90f -> CoralRed
                        percentage > 75f -> AmberWarning
                        else -> PrimaryBlue
                    }

                    drawArc(
                        color = arcColor,
                        startAngle = 135f,
                        sweepAngle = 270f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Used Space",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Used vs Total label
            Text(
                text = "${stats.formattedUsed} / ${stats.formattedTotal} Used",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "${stats.formattedFree} Available Storage",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Prominent "Smart Scan Now" Primary Button
            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.hasCompletedScan) "Rescan Gallery" else "Smart Scan Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    count: Int,
    sizeFormatted: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = sizeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = if (count > 0) "$count items detected" else "Tap to scan & review",
                style = MaterialTheme.typography.bodySmall,
                color = if (count > 0) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun LifetimeSavedCard(lifetimeSavedFormatted: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F291E)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total Space Saved with SmartClean",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA7F3D0)
                )
                Text(
                    text = lifetimeSavedFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AdMobBannerPlaceholder() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(1.dp, OutlineDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SmartClean AI • 100% On-Device & Private Gallery Protection",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}
