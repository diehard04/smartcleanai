package com.diehard.smartcleanai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_media",
    indices = [
        Index(value = ["dateModified"]),
        Index(value = ["category"]),
        Index(value = ["groupId"]),
        Index(value = ["isIgnored"])
    ]
)
data class ScannedMediaEntity(
    @PrimaryKey
    val mediaId: Long,
    val uri: String,
    val displayName: String?,
    val bucketName: String,
    val fileSize: Long,
    val dateModified: Long,
    val mimeType: String?,
    val width: Int = 0,
    val height: Int = 0,
    val pHash: Long? = null,
    val blurScore: Float? = null,
    val category: String, // DUPLICATE, BLURRY, WHATSAPP_JUNK, SCREENSHOT, LARGE_VIDEO, CLEAN
    val groupId: String? = null,
    val isBestShot: Boolean = false,
    val isIgnored: Boolean = false,
    val sha256: String? = null
)
