package com.example.smartcleanai.ui.viewmodel

import android.app.PendingIntent
import com.example.smartcleanai.data.media.StorageStats
import com.example.smartcleanai.domain.model.MediaCategory
import com.example.smartcleanai.domain.model.MediaItemModel
import com.example.smartcleanai.domain.model.ScanCategoryResult
import com.example.smartcleanai.domain.model.ScanProgress

data class MainUiState(
    val storageStats: StorageStats = StorageStats(0L, 0L, 0L),
    val scanProgress: ScanProgress = ScanProgress(),
    val scanResults: Map<MediaCategory, ScanCategoryResult> = emptyMap(),
    val selectedItemIds: Set<Long> = emptySet(),
    val bestShotItemIds: Set<Long> = emptySet(),
    val currentCompareItem: MediaItemModel? = null,
    val lifetimeBytesSaved: Long = 0L,
    val blurSensitivity: Float = 100.0f,
    val isAutoScanEnabled: Boolean = true,
    val ignoredItems: List<MediaItemModel> = emptyList(),
    val pendingDeleteIntent: PendingIntent? = null,
    val pendingDeleteIds: List<Long> = emptyList(),
    val pendingDeleteBytes: Long = 0L,
    val isScanning: Boolean = false,
    val hasCompletedScan: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val cleanSuccessMessage: String? = null
) {
    val totalReclaimableBytes: Long
        get() = scanResults.values.sumOf { it.totalBytes }

    val formattedTotalReclaimable: String
        get() {
            val mb = totalReclaimableBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                else -> String.format("%.0f MB", mb)
            }
        }

    val selectedBytesCount: Long
        get() {
            var sum = 0L
            for (categoryResult in scanResults.values) {
                for (item in categoryResult.items) {
                    if (selectedItemIds.contains(item.mediaId)) {
                        sum += item.fileSize
                    }
                }
                for (group in categoryResult.duplicateGroups) {
                    for (cand in group.duplicateCandidates) {
                        if (selectedItemIds.contains(cand.mediaId)) {
                            sum += cand.fileSize
                        }
                    }
                }
            }
            return sum
        }

    val formattedSelectedBytes: String
        get() {
            val mb = selectedBytesCount / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                else -> String.format("%.1f MB", mb)
            }
        }

    val formattedLifetimeSaved: String
        get() {
            val mb = lifetimeBytesSaved / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.1f GB", gb)
                else -> String.format("%.0f MB", mb)
            }
        }
}
