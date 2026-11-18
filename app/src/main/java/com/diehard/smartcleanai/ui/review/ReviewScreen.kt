package com.diehard.smartcleanai.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diehard.smartcleanai.domain.model.DuplicateGroup
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.domain.model.MediaItemModel
import com.diehard.smartcleanai.ui.theme.BgDark
import com.diehard.smartcleanai.ui.theme.CardDark
import com.diehard.smartcleanai.ui.theme.CoralRed
import com.diehard.smartcleanai.ui.theme.ElectricCyan
import com.diehard.smartcleanai.ui.theme.EmeraldGreen
import com.diehard.smartcleanai.ui.theme.OutlineDark
import com.diehard.smartcleanai.ui.theme.SurfaceDark
import com.diehard.smartcleanai.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    initialCategory: MediaCategory,
    uiState: MainUiState,
    onBack: () -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: (MediaCategory, Boolean) -> Unit,
    onSetBestShot: (String, Long) -> Unit,
    onOpenCompare: (MediaItemModel) -> Unit,
    onCloseCompare: () -> Unit,
    onToggleIgnore: (MediaItemModel) -> Unit,
    onCleanNow: (MediaCategory) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val categories = listOf(
        MediaCategory.DUPLICATE,
        MediaCategory.BLURRY,
        MediaCategory.WHATSAPP_JUNK,
        MediaCategory.SCREENSHOT,
        MediaCategory.LARGE_VIDEO
    )

    val currentResult = uiState.scanResults[selectedCategory]

    // Calculate selected bytes in the currently viewed category
    val currentSelectedBytes = remember(uiState.selectedItemIds, currentResult) {
        if (currentResult == null) return@remember 0L
        var total = 0L
        if (selectedCategory == MediaCategory.DUPLICATE) {
            for (group in currentResult.duplicateGroups) {
                for (cand in group.duplicateCandidates) {
                    if (uiState.selectedItemIds.contains(cand.mediaId)) {
                        total += cand.fileSize
                    }
                }
            }
        } else {
            for (item in currentResult.items) {
                if (uiState.selectedItemIds.contains(item.mediaId)) {
                    total += item.fileSize
                }
            }
        }
        total
    }

    val selectedMb = currentSelectedBytes / (1024.0 * 1024.0)
    val selectedGb = selectedMb / 1024.0
    val formattedCategorySelectedSize = if (selectedGb >= 1.0) {
        String.format("%.2f GB", selectedGb)
    } else {
        String.format("%.0f MB", selectedMb)
    }

    // Check if all selectable items in current category are selected
    val isAllSelected = remember(uiState.selectedItemIds, currentResult) {
        if (currentResult == null) return@remember false
        val selectableIds = if (selectedCategory == MediaCategory.DUPLICATE) {
            currentResult.duplicateGroups.flatMap { it.duplicateCandidates }.map { it.mediaId }
        } else {
            currentResult.items.map { it.mediaId }
        }
        selectableIds.isNotEmpty() && selectableIds.all { uiState.selectedItemIds.contains(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedCategory.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar
            ReviewBottomActionBar(
                isAllSelected = isAllSelected,
                selectedBytes = currentSelectedBytes,
                formattedSelectedSize = formattedCategorySelectedSize,
                onToggleSelectAll = {
                    onSelectAll(selectedCategory, !isAllSelected)
                },
                onClean = {
                    onCleanNow(selectedCategory)
                }
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Switcher Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                containerColor = SurfaceDark,
                contentColor = ElectricCyan,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val index = categories.indexOf(selectedCategory).coerceAtLeast(0)
                    if (index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = ElectricCyan,
                            height = 3.dp
                        )
                    }
                }
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    val result = uiState.scanResults[category]
                    val count = result?.itemCount ?: 0

                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.title.split(" ").first(),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) ElectricCyan else Color(0xFF334155))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Main Content Area
            if (currentResult == null || currentResult.itemCount == 0) {
                EmptyCategoryPlaceholder(selectedCategory)
            } else {
                if (selectedCategory == MediaCategory.DUPLICATE) {
                    // Grouped View for Duplicates & Similar Photos
                    DuplicateGroupedListView(
                        groups = currentResult.duplicateGroups,
                        selectedItemIds = uiState.selectedItemIds,
                        onToggleSelect = onToggleSelect,
                        onPhotoClick = onOpenCompare,
                        onSetBestShot = onSetBestShot
                    )
                } else {
                    // 3-Column Grid View for Blurry, WhatsApp, Screenshots, Large Videos
                    CategoryGridView(
                        items = currentResult.items,
                        selectedItemIds = uiState.selectedItemIds,
                        onToggleSelect = onToggleSelect,
                        onPhotoClick = onOpenCompare
                    )
                }
            }
        }
    }

    // Full Screen Compare & Inspection Modal
    if (uiState.currentCompareItem != null) {
        val compareItem = uiState.currentCompareItem
        val isSelected = uiState.selectedItemIds.contains(compareItem.mediaId)
        val parentGroup = currentResult?.duplicateGroups?.firstOrNull { group ->
            group.bestShot.mediaId == compareItem.mediaId ||
                    group.duplicateCandidates.any { it.mediaId == compareItem.mediaId }
        }

        PhotoCompareDialog(
            item = compareItem,
            isSelectedForDeletion = isSelected,
            onDismiss = onCloseCompare,
            onToggleSelection = { onToggleSelect(compareItem.mediaId) },
            onToggleIgnore = { onToggleIgnore(compareItem) },
            onSetAsBestShot = if (parentGroup != null && !compareItem.isBestShot) {
                {
                    onSetBestShot(parentGroup.groupId, compareItem.mediaId)
                    onCloseCompare()
                }
            } else null
        )
    }
}

@Composable
fun DuplicateGroupedListView(
    groups: List<DuplicateGroup>,
    selectedItemIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onPhotoClick: (MediaItemModel) -> Unit,
    onSetBestShot: (String, Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(groups, key = { it.groupId }) { group ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Cluster Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${group.duplicateCandidates.size + 1} Similar Photos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Save ${group.duplicateCandidates.filter { selectedItemIds.contains(it.mediaId) }.sumOf { it.fileSize } / (1024 * 1024)} MB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal list of photos in this cluster
                    val allClusterItems = listOf(group.bestShot) + group.duplicateCandidates

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allClusterItems, key = { it.mediaId }) { item ->
                            val isBest = item.mediaId == group.bestShot.mediaId
                            val isChecked = selectedItemIds.contains(item.mediaId)

                            DuplicatePhotoThumbnail(
                                item = item,
                                isBestShot = isBest,
                                isChecked = isChecked,
                                onThumbnailClick = { onPhotoClick(item) },
                                onCheckboxClick = {
                                    if (!isBest) {
                                        onToggleSelect(item.mediaId)
                                    }
                                },
                                onSetBestShot = if (!isBest) {
                                    { onSetBestShot(group.groupId, item.mediaId) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicatePhotoThumbnail(
    item: MediaItemModel,
    isBestShot: Boolean,
    isChecked: Boolean,
    onThumbnailClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    onSetBestShot: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(
                width = if (isBestShot) 2.dp else if (isChecked) 2.dp else 1.dp,
                color = if (isBestShot) EmeraldGreen else if (isChecked) CoralRed else OutlineDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onThumbnailClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top Badges
        if (isBestShot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldGreen)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Best Shot",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            // Checkbox on top-right for duplicate candidates
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isChecked) CoralRed else Color.Black.copy(alpha = 0.5f))
                    .clickable { onCheckboxClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom File Size Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.formattedSize,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun CategoryGridView(
    items: List<MediaItemModel>,
    selectedItemIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onPhotoClick: (MediaItemModel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.mediaId }) { item ->
            val isChecked = selectedItemIds.contains(item.mediaId)

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .border(
                        width = if (isChecked) 2.dp else 1.dp,
                        color = if (isChecked) CoralRed else OutlineDark,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onPhotoClick(item) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Video indicator if video
                if (item.mimeType.startsWith("video/")) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Selection Checkbox on top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isChecked) CoralRed else Color.Black.copy(alpha = 0.5f))
                        .clickable { onToggleSelect(item.mediaId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // File Size at bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewBottomActionBar(
    isAllSelected: Boolean,
    selectedBytes: Long,
    formattedSelectedSize: String,
    onToggleSelectAll: () -> Unit,
    onClean: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Select All / Deselect All
            OutlinedButton(
                onClick = onToggleSelectAll,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF475569))
                )
            ) {
                Text(
                    text = if (isAllSelected) "Deselect All" else "Select All",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // High-Contrast Red "Clean Now" Button
            Button(
                onClick = onClean,
                enabled = selectedBytes > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    disabledContainerColor = Color(0xFF334155)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedBytes > 0) "Clean Now ($formattedSelectedSize)" else "Select Items",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyCategoryPlaceholder(category: MediaCategory) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No Junk Detected!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Your photos in ${category.title} are completely clean.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
