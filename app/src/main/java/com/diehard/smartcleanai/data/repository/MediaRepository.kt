package com.diehard.smartcleanai.data.repository

import android.app.PendingIntent
import com.diehard.smartcleanai.data.media.StorageStats
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.domain.model.MediaItemModel
import com.diehard.smartcleanai.domain.model.ScanCategoryResult
import com.diehard.smartcleanai.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun scanGallery(
        isIncremental: Boolean = true,
        onProgress: (ScanProgress) -> Unit
    ): Flow<Map<MediaCategory, ScanCategoryResult>>

    fun getStorageStats(): StorageStats

    suspend fun setIgnoreStatus(mediaId: Long, isIgnored: Boolean)

    fun getIgnoredItems(): Flow<List<MediaItemModel>>

    fun prepareTrashOrDeleteRequest(
        mediaIds: List<Long>,
        isPermanent: Boolean = false
    ): PendingIntent?

    suspend fun executeLegacyDelete(mediaIds: List<Long>): Int

    suspend fun onItemsDeleted(deletedIds: List<Long>, freedBytes: Long)

    fun getLifetimeBytesSaved(): Long

    fun setBlurSensitivity(threshold: Float)

    fun getBlurSensitivity(): Float
}
