package com.diehard.smartcleanai.domain.model

import android.net.Uri

data class MediaItemModel(
    val mediaId: Long,
    val uri: Uri,
    val displayName: String,
    val bucketName: String,
    val fileSize: Long,
    val dateModified: Long,
    val mimeType: String,
    val width: Int = 0,
    val height: Int = 0,
    val pHash: Long? = null,
    val blurScore: Float? = null,
    val category: MediaCategory = MediaCategory.CLEAN,
    val groupId: String? = null,
    val isBestShot: Boolean = false,
    val isIgnored: Boolean = false,
    val isSelected: Boolean = false,
    val similarityReason: String? = null
) {
    val formattedSize: String
        get() {
            val kb = fileSize / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }

    val resolutionFormatted: String
        get() = if (width > 0 && height > 0) "${width}x${height}" else "Unknown"
}
