package com.diehard.smartcleanai.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diehard.smartcleanai.data.repository.MediaRepository
import com.diehard.smartcleanai.data.repository.MediaRepositoryImpl
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.domain.model.MediaItemModel
import com.diehard.smartcleanai.worker.GalleryScanWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: MediaRepository
) : AndroidViewModel(application) {

    // Secondary constructor with single Application argument required by ViewModelProvider.AndroidViewModelFactory reflection
    constructor(application: Application) : this(
        application,
        MediaRepositoryImpl(application)
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        refreshStorageStats()
        loadInitialData()
    }

    fun refreshStorageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = repository.getStorageStats()
            val lifetimeSaved = repository.getLifetimeBytesSaved()
            val sensitivity = repository.getBlurSensitivity()

            _uiState.update {
                it.copy(
                    storageStats = stats,
                    lifetimeBytesSaved = lifetimeSaved,
                    blurSensitivity = sensitivity
                )
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getIgnoredItems().collect { ignored ->
                _uiState.update { it.copy(ignoredItems = ignored) }
            }
        }
    }

    fun onPermissionsResult(isGranted: Boolean) {
        _uiState.update { it.copy(isPermissionGranted = isGranted) }
        if (isGranted) {
            refreshStorageStats()
        }
    }

    fun startScan(isIncremental: Boolean = true) {
        scanJob?.cancel()
        _uiState.update {
            it.copy(
                isScanning = true,
                hasCompletedScan = false
            )
        }

        scanJob = viewModelScope.launch {
            try {
                repository.scanGallery(
                    isIncremental = isIncremental,
                    onProgress = { progress ->
                        _uiState.update { it.copy(scanProgress = progress) }
                    }
                ).collect { results ->
                    // Collect pre-selected item IDs and best shot IDs
                    val selectedIds = mutableSetOf<Long>()
                    val bestShots = mutableSetOf<Long>()

                    for ((_, categoryResult) in results) {
                        for (item in categoryResult.items) {
                            if (item.isSelected) selectedIds.add(item.mediaId)
                        }
                        for (group in categoryResult.duplicateGroups) {
                            bestShots.add(group.bestShot.mediaId)
                            for (candidate in group.duplicateCandidates) {
                                if (candidate.isSelected) selectedIds.add(candidate.mediaId)
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            scanResults = results,
                            selectedItemIds = selectedIds,
                            bestShotItemIds = bestShots,
                            isScanning = false,
                            hasCompletedScan = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _uiState.update {
            it.copy(
                isScanning = false,
                scanProgress = it.scanProgress.copy(isScanning = false)
            )
        }
    }

    fun toggleItemSelection(mediaId: Long) {
        _uiState.update { state ->
            val updated = state.selectedItemIds.toMutableSet()
            if (updated.contains(mediaId)) {
                updated.remove(mediaId)
            } else {
                updated.add(mediaId)
            }
            state.copy(selectedItemIds = updated)
        }
    }

    fun selectAll(category: MediaCategory, select: Boolean) {
        _uiState.update { state ->
            val updated = state.selectedItemIds.toMutableSet()
            val categoryResult = state.scanResults[category]

            if (categoryResult != null) {
                if (category == MediaCategory.DUPLICATE) {
                    for (group in categoryResult.duplicateGroups) {
                        for (candidate in group.duplicateCandidates) {
                            if (select) updated.add(candidate.mediaId) else updated.remove(candidate.mediaId)
                        }
                    }
                } else {
                    for (item in categoryResult.items) {
                        if (select) updated.add(item.mediaId) else updated.remove(item.mediaId)
                    }
                }
            }

            state.copy(selectedItemIds = updated)
        }
    }

    fun setBestShot(groupId: String, newBestShotId: Long) {
        _uiState.update { state ->
            val dupCategory = state.scanResults[MediaCategory.DUPLICATE] ?: return@update state
            val updatedGroups = dupCategory.duplicateGroups.map { group ->
                if (group.groupId == groupId) {
                    val allGroupItems = listOf(group.bestShot) + group.duplicateCandidates
                    val newBest = allGroupItems.firstOrNull { it.mediaId == newBestShotId } ?: group.bestShot
                    val newDuplicates = allGroupItems.filter { it.mediaId != newBestShotId }.map {
                        it.copy(isBestShot = false, isSelected = true)
                    }
                    group.copy(
                        bestShot = newBest.copy(isBestShot = true, isSelected = false),
                        duplicateCandidates = newDuplicates
                    )
                } else {
                    group
                }
            }

            val updatedSelectedIds = state.selectedItemIds.toMutableSet().apply {
                remove(newBestShotId)
                // Add new candidates
                val group = updatedGroups.firstOrNull { it.groupId == groupId }
                group?.duplicateCandidates?.forEach { add(it.mediaId) }
            }

            val updatedBestShotIds = state.bestShotItemIds.toMutableSet().apply {
                val oldBest = dupCategory.duplicateGroups.firstOrNull { it.groupId == groupId }?.bestShot?.mediaId
                if (oldBest != null) remove(oldBest)
                add(newBestShotId)
            }

            val updatedDupCategory = dupCategory.copy(duplicateGroups = updatedGroups)
            val updatedResults = state.scanResults.toMutableMap().apply {
                put(MediaCategory.DUPLICATE, updatedDupCategory)
            }

            state.copy(
                scanResults = updatedResults,
                selectedItemIds = updatedSelectedIds,
                bestShotItemIds = updatedBestShotIds
            )
        }
    }

    fun openCompareDialog(item: MediaItemModel) {
        _uiState.update { it.copy(currentCompareItem = item) }
    }

    fun closeCompareDialog() {
        _uiState.update { it.copy(currentCompareItem = null) }
    }

    fun toggleIgnoreItem(item: MediaItemModel) {
        viewModelScope.launch {
            val newStatus = !item.isIgnored
            repository.setIgnoreStatus(item.mediaId, newStatus)

            // Remove from current selected / results if ignored
            if (newStatus) {
                _uiState.update { state ->
                    val selected = state.selectedItemIds.toMutableSet().apply { remove(item.mediaId) }
                    state.copy(selectedItemIds = selected, currentCompareItem = null)
                }
            }
        }
    }

    fun initiateClean(category: MediaCategory? = null, isPermanent: Boolean = false) {
        val state = _uiState.value
        val targetIds = mutableListOf<Long>()
        var targetBytes = 0L

        if (category != null) {
            val catResult = state.scanResults[category]
            if (catResult != null) {
                if (category == MediaCategory.DUPLICATE) {
                    for (group in catResult.duplicateGroups) {
                        for (cand in group.duplicateCandidates) {
                            if (state.selectedItemIds.contains(cand.mediaId)) {
                                targetIds.add(cand.mediaId)
                                targetBytes += cand.fileSize
                            }
                        }
                    }
                } else {
                    for (item in catResult.items) {
                        if (state.selectedItemIds.contains(item.mediaId)) {
                            targetIds.add(item.mediaId)
                            targetBytes += item.fileSize
                        }
                    }
                }
            }
        } else {
            // Clean all selected items across categories
            for (catResult in state.scanResults.values) {
                for (item in catResult.items) {
                    if (state.selectedItemIds.contains(item.mediaId)) {
                        targetIds.add(item.mediaId)
                        targetBytes += item.fileSize
                    }
                }
                for (group in catResult.duplicateGroups) {
                    for (cand in group.duplicateCandidates) {
                        if (state.selectedItemIds.contains(cand.mediaId)) {
                            targetIds.add(cand.mediaId)
                            targetBytes += cand.fileSize
                        }
                    }
                }
            }
        }

        if (targetIds.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = repository.prepareTrashOrDeleteRequest(targetIds, isPermanent)
            _uiState.update {
                it.copy(
                    pendingDeleteIntent = intent,
                    pendingDeleteIds = targetIds,
                    pendingDeleteBytes = targetBytes
                )
            }
        } else {
            // Android 10 and below: execute direct delete
            viewModelScope.launch {
                val deletedCount = repository.executeLegacyDelete(targetIds)
                if (deletedCount > 0) {
                    onDeleteConfirmed(success = true, targetIds, targetBytes)
                }
            }
        }
    }

    fun onDeleteConfirmed(
        success: Boolean,
        confirmedIds: List<Long> = _uiState.value.pendingDeleteIds,
        confirmedBytes: Long = _uiState.value.pendingDeleteBytes
    ) {
        if (!success || confirmedIds.isEmpty()) {
            _uiState.update {
                it.copy(
                    pendingDeleteIntent = null,
                    pendingDeleteIds = emptyList(),
                    pendingDeleteBytes = 0L
                )
            }
            return
        }

        viewModelScope.launch {
            repository.onItemsDeleted(confirmedIds, confirmedBytes)
            val deletedSet = confirmedIds.toSet()

            _uiState.update { state ->
                val newSelected = state.selectedItemIds.filter { it !in deletedSet }.toSet()

                // Filter deleted items out of scan results
                val newScanResults = state.scanResults.mapValues { (_, catResult) ->
                    val filteredItems = catResult.items.filter { it.mediaId !in deletedSet }
                    val filteredGroups = catResult.duplicateGroups.mapNotNull { group ->
                        val filteredCandidates = group.duplicateCandidates.filter { it.mediaId !in deletedSet }
                        if (filteredCandidates.isEmpty() && group.bestShot.mediaId in deletedSet) {
                            null
                        } else {
                            group.copy(duplicateCandidates = filteredCandidates)
                        }
                    }
                    catResult.copy(
                        items = filteredItems,
                        duplicateGroups = filteredGroups,
                        totalBytes = filteredItems.sumOf { it.fileSize } + filteredGroups.sumOf { it.totalReclaimableBytes },
                        itemCount = filteredItems.size + filteredGroups.sumOf { it.duplicateCandidates.size }
                    )
                }

                state.copy(
                    scanResults = newScanResults,
                    selectedItemIds = newSelected,
                    pendingDeleteIntent = null,
                    pendingDeleteIds = emptyList(),
                    pendingDeleteBytes = 0L,
                    lifetimeBytesSaved = state.lifetimeBytesSaved + confirmedBytes
                )
            }
            refreshStorageStats()
        }
    }

    fun clearPendingDeleteIntent() {
        _uiState.update { it.copy(pendingDeleteIntent = null) }
    }

    fun updateBlurSensitivity(threshold: Float) {
        repository.setBlurSensitivity(threshold)
        _uiState.update { it.copy(blurSensitivity = threshold) }
    }

    fun toggleAutoScan(enabled: Boolean) {
        _uiState.update { it.copy(isAutoScanEnabled = enabled) }
        if (enabled) {
            GalleryScanWorker.enqueuePeriodicScan(getApplication())
        } else {
            GalleryScanWorker.cancelPeriodicScan(getApplication())
        }
    }

    fun setProUser(isPro: Boolean) {
        _uiState.update { it.copy(isProUser = isPro) }
    }
}
