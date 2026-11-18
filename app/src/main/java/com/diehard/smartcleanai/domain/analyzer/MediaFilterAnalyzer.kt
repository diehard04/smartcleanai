package com.diehard.smartcleanai.domain.analyzer

import com.diehard.smartcleanai.data.media.RawMediaItem
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.domain.model.MediaItemModel
import java.util.Locale
import java.util.concurrent.TimeUnit

class MediaFilterAnalyzer {

    companion object {
        val THIRTY_DAYS_MILLIS = TimeUnit.DAYS.toMillis(30)
        const val LARGE_VIDEO_BYTES_THRESHOLD = 50L * 1024 * 1024 // 50 MB
    }

    fun isOldScreenshot(bucketName: String, displayName: String, dateModifiedSeconds: Long): Boolean {
        val isScreenshotName = bucketName.lowercase(Locale.ROOT).contains("screenshot") ||
                displayName.lowercase(Locale.ROOT).startsWith("screenshot")

        if (!isScreenshotName) return false

        // dateModified from MediaStore is in seconds
        val itemTimeMillis = dateModifiedSeconds * 1000L
        val ageMillis = System.currentTimeMillis() - itemTimeMillis
        return ageMillis >= THIRTY_DAYS_MILLIS
    }

    fun isLargeVideo(isVideo: Boolean, mimeType: String, sizeBytes: Long): Boolean {
        val isVideoType = isVideo || mimeType.lowercase(Locale.ROOT).startsWith("video/")
        return isVideoType && sizeBytes >= LARGE_VIDEO_BYTES_THRESHOLD
    }

    fun filterOldScreenshots(rawItems: List<RawMediaItem>): List<MediaItemModel> {
        return rawItems
            .filter { !it.isVideo && isOldScreenshot(it.bucketName, it.displayName, it.dateModified) }
            .map {
                val ageDays = (System.currentTimeMillis() - (it.dateModified * 1000L)) / (1000 * 60 * 60 * 24)
                MediaItemModel(
                    mediaId = it.id,
                    uri = it.uri,
                    displayName = it.displayName,
                    bucketName = it.bucketName,
                    fileSize = it.size,
                    dateModified = it.dateModified,
                    mimeType = it.mimeType,
                    width = it.width,
                    height = it.height,
                    category = MediaCategory.SCREENSHOT,
                    isSelected = true,
                    similarityReason = "Captured $ageDays days ago"
                )
            }
    }

    fun filterLargeVideos(rawItems: List<RawMediaItem>): List<MediaItemModel> {
        return rawItems
            .filter { isLargeVideo(it.isVideo, it.mimeType, it.size) }
            .sortedByDescending { it.size }
            .map {
                MediaItemModel(
                    mediaId = it.id,
                    uri = it.uri,
                    displayName = it.displayName,
                    bucketName = it.bucketName,
                    fileSize = it.size,
                    dateModified = it.dateModified,
                    mimeType = it.mimeType,
                    width = it.width,
                    height = it.height,
                    category = MediaCategory.LARGE_VIDEO,
                    isSelected = false, // Unselected by default so user reviews heavy videos before deleting
                    similarityReason = "Large Video File"
                )
            }
    }
}
