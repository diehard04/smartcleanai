package com.diehard.smartcleanai.data.media

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class RawMediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val bucketName: String,
    val size: Long,
    val dateModified: Long,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val isVideo: Boolean
)

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long
) {
    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) * 100f else 0f

    val formattedTotal: String
        get() = formatBytes(totalBytes)

    val formattedUsed: String
        get() = formatBytes(usedBytes)

    val formattedFree: String
        get() = formatBytes(freeBytes)

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return String.format("%.1f GB", gb)
    }
}

class MediaStoreDataSource(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun queryAllMedia(): List<RawMediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<RawMediaItem>()

        // 1. Query Images
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        contentResolver.query(
            imageUri,
            imageProjection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(imageUri, id)
                val displayName = cursor.getString(nameCol) ?: "image_$id"
                val bucketName = cursor.getString(bucketCol) ?: "Default"
                val size = cursor.getLong(sizeCol)
                val dateModified = cursor.getLong(dateCol)
                val mimeType = cursor.getString(mimeCol) ?: "image/jpeg"
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)

                // Skip corrupted or 0-byte files
                if (size > 0) {
                    mediaList.add(
                        RawMediaItem(
                            id = id,
                            uri = uri,
                            displayName = displayName,
                            bucketName = bucketName,
                            size = size,
                            dateModified = dateModified,
                            mimeType = mimeType,
                            width = width,
                            height = height,
                            isVideo = false
                        )
                    )
                }
            }
        }

        // 2. Query Videos
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        contentResolver.query(
            videoUri,
            videoProjection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(videoUri, id)
                val displayName = cursor.getString(nameCol) ?: "video_$id"
                val bucketName = cursor.getString(bucketCol) ?: "Video"
                val size = cursor.getLong(sizeCol)
                val dateModified = cursor.getLong(dateCol)
                val mimeType = cursor.getString(mimeCol) ?: "video/mp4"
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)

                if (size > 0) {
                    mediaList.add(
                        RawMediaItem(
                            id = id,
                            uri = uri,
                            displayName = displayName,
                            bucketName = bucketName,
                            size = size,
                            dateModified = dateModified,
                            mimeType = mimeType,
                            width = width,
                            height = height,
                            isVideo = true
                        )
                    )
                }
            }
        }

        mediaList
    }

    suspend fun loadDownscaledBitmap(uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        return@withContext contentResolver.loadThumbnail(
                            uri,
                            Size(targetWidth, targetHeight),
                            null
                        )
                    } catch (_: Exception) {
                        // Fallback to BitmapFactory
                    }
                }

                // Decode bounds first
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }

                if (options.outWidth <= 0 || options.outHeight <= 0) return@withContext null

                // Compute inSampleSize
                var inSampleSize = 1
                while (options.outWidth / (inSampleSize * 2) >= targetWidth &&
                    options.outHeight / (inSampleSize * 2) >= targetHeight
                ) {
                    inSampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }

                contentResolver.openInputStream(uri)?.use { stream ->
                    val sampledBitmap = BitmapFactory.decodeStream(stream, null, decodeOptions) ?: return@use null
                    Bitmap.createScaledBitmap(sampledBitmap, targetWidth, targetHeight, false)
                }
            } catch (e: Exception) {
                null
            }
        }

    suspend fun computeSha256(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                val hashBytes = digest.digest()
                hashBytes.joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getDeviceStorageStats(): StorageStats {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes

            StorageStats(totalBytes = totalBytes, freeBytes = freeBytes, usedBytes = usedBytes)
        } catch (e: Exception) {
            StorageStats(128L * 1024 * 1024 * 1024, 32L * 1024 * 1024 * 1024, 96L * 1024 * 1024 * 1024)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createTrashRequest(uris: List<Uri>, isTrash: Boolean): PendingIntent {
        return MediaStore.createTrashRequest(contentResolver, uris, isTrash)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(uris: List<Uri>): PendingIntent {
        return MediaStore.createDeleteRequest(contentResolver, uris)
    }

    suspend fun deleteLegacy(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (uri in uris) {
            try {
                count += contentResolver.delete(uri, null, null)
            } catch (_: Exception) {
            }
        }
        count
    }
}
