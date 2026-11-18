package com.diehard.smartcleanai.data.repository

import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.diehard.smartcleanai.data.local.AppDatabase
import com.diehard.smartcleanai.data.local.ScannedMediaEntity
import com.diehard.smartcleanai.data.media.MediaStoreDataSource
import com.diehard.smartcleanai.data.media.RawMediaItem
import com.diehard.smartcleanai.data.media.StorageStats
import com.diehard.smartcleanai.domain.analyzer.BlurDetector
import com.diehard.smartcleanai.domain.analyzer.DuplicateAnalyzer
import com.diehard.smartcleanai.domain.analyzer.MediaFilterAnalyzer
import com.diehard.smartcleanai.domain.analyzer.WhatsAppJunkClassifier
import com.diehard.smartcleanai.domain.model.MediaCategory
import com.diehard.smartcleanai.domain.model.MediaItemModel
import com.diehard.smartcleanai.domain.model.ScanCategoryResult
import com.diehard.smartcleanai.domain.model.ScanProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class MediaRepositoryImpl(
    private val context: Context,
    private val mediaStoreDataSource: MediaStoreDataSource = MediaStoreDataSource(context),
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val duplicateAnalyzer: DuplicateAnalyzer = DuplicateAnalyzer(),
    private val blurDetector: BlurDetector = BlurDetector(),
    private val whatsAppClassifier: WhatsAppJunkClassifier = WhatsAppJunkClassifier(),
    private val filterAnalyzer: MediaFilterAnalyzer = MediaFilterAnalyzer()
) : MediaRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_clean_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LIFETIME_SAVED_BYTES = "key_lifetime_saved_bytes"
        private const val KEY_BLUR_SENSITIVITY = "key_blur_sensitivity"
        private const val BATCH_PERSIST_SIZE = 40
    }

    override fun getStorageStats(): StorageStats {
        return mediaStoreDataSource.getDeviceStorageStats()
    }

    override fun getLifetimeBytesSaved(): Long {
        return prefs.getLong(KEY_LIFETIME_SAVED_BYTES, 0L)
    }

    override fun setBlurSensitivity(threshold: Float) {
        prefs.edit().putFloat(KEY_BLUR_SENSITIVITY, threshold).apply()
        blurDetector.setSensitivityThreshold(threshold)
    }

    override fun getBlurSensitivity(): Float {
        return prefs.getFloat(KEY_BLUR_SENSITIVITY, BlurDetector.DEFAULT_MEDIUM_THRESHOLD)
    }

    override suspend fun setIgnoreStatus(mediaId: Long, isIgnored: Boolean) {
        database.scannedMediaDao().updateIgnored(mediaId, isIgnored)
    }

    override fun getIgnoredItems(): Flow<List<MediaItemModel>> {
        return database.scannedMediaDao().getIgnoredMedia().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun scanGallery(
        isIncremental: Boolean,
        onProgress: (ScanProgress) -> Unit
    ): Flow<Map<MediaCategory, ScanCategoryResult>> = flow {
        blurDetector.setSensitivityThreshold(getBlurSensitivity())

        // 1. Fetch all raw media from MediaStore
        val rawMedia = mediaStoreDataSource.queryAllMedia()
        val totalCount = rawMedia.size

        onProgress(
            ScanProgress(
                current = 0,
                total = totalCount,
                currentFileName = "Indexing storage...",
                junkBytesFound = 0L,
                isScanning = true
            )
        )

        // 2. Fetch cached items from Room DB
        val dao = database.scannedMediaDao()
        val cacheMap = if (isIncremental) {
            dao.getAllCacheChecks().associate { it.mediaId to it.dateModified }
        } else {
            emptyMap()
        }

        val cachedEntitiesMap = if (isIncremental) {
            dao.getAllEntitiesDirect().associateBy { it.mediaId }
        } else {
            emptyMap()
        }

        // Find potential exact duplicate candidates by file size
        val sizeGrouping = rawMedia.groupBy { it.size }
        val sizeDuplicateCandidates = sizeGrouping.filter { it.value.size > 1 }.values.flatten().map { it.id }.toSet()
        val exactHashes = ConcurrentHashMap<Long, String>()

        // 3. Multi-Core Concurrent Parallel Pipeline bounded by available CPU cores
        val parallelism = (Runtime.getRuntime().availableProcessors()).coerceIn(2, 6)
        val semaphore = Semaphore(parallelism)
        val counter = AtomicInteger(0)
        val junkAccumulator = AtomicLong(0L)
        val entitiesToPersist = Collections.synchronizedList(mutableListOf<ScannedMediaEntity>())

        val allProcessedModels = coroutineScope {
            rawMedia.map { item ->
                async(Dispatchers.Default) {
                    val isCached = cacheMap[item.id] == item.dateModified
                    val cachedEntity = cachedEntitiesMap[item.id]

                    val model: MediaItemModel
                    if (isCached && cachedEntity != null) {
                        // Instant incremental cache hit
                        model = cachedEntity.toModel()
                        if (cachedEntity.sha256 != null) {
                            exactHashes[item.id] = cachedEntity.sha256
                        }
                    } else {
                        // Concurrent vision analysis bounded by semaphore
                        model = semaphore.withPermit {
                            val analyzed = analyzeNewItem(
                                item,
                                sizeDuplicateCandidates.contains(item.id),
                                exactHashes
                            )
                            val entity = analyzed.toEntity(exactHashes[item.id])
                            entitiesToPersist.add(entity)

                            // Periodic incremental batch save so progress is not lost
                            if (entitiesToPersist.size >= BATCH_PERSIST_SIZE) {
                                val chunkToInsert = synchronized(entitiesToPersist) {
                                    val copy = ArrayList(entitiesToPersist)
                                    entitiesToPersist.clear()
                                    copy
                                }
                                if (chunkToInsert.isNotEmpty()) {
                                    dao.insertAll(chunkToInsert)
                                }
                            }
                            analyzed
                        }
                    }

                    val processedCount = counter.incrementAndGet()
                    if (model.category != MediaCategory.CLEAN && !model.isIgnored) {
                        junkAccumulator.addAndGet(model.fileSize)
                    }

                    if (processedCount % 15 == 0 || processedCount == totalCount) {
                        onProgress(
                            ScanProgress(
                                current = processedCount,
                                total = totalCount,
                                currentFileName = item.displayName,
                                junkBytesFound = junkAccumulator.get(),
                                isScanning = true
                            )
                        )
                    }

                    model
                }
            }.awaitAll()
        }

        // 4. Batch persist any remaining entities to Room DB
        val remainingEntities = synchronized(entitiesToPersist) {
            val copy = ArrayList(entitiesToPersist)
            entitiesToPersist.clear()
            copy
        }
        if (remainingEntities.isNotEmpty()) {
            dao.insertAll(remainingEntities)
        }

        // 5. Exclude ignored / whitelisted media from results
        val activeModels = allProcessedModels.filter { !it.isIgnored }

        // 6. Cluster duplicates & visually similar burst shots
        val imageModels = activeModels.filter { it.mimeType.startsWith("image/") }
        val duplicateGroups = duplicateAnalyzer.groupDuplicates(imageModels, exactHashes)
        val duplicateItemIds = duplicateGroups.flatMap { group ->
            group.duplicateCandidates.map { it.mediaId } + group.bestShot.mediaId
        }.toSet()

        val duplicateCategoryResult = ScanCategoryResult(
            category = MediaCategory.DUPLICATE,
            items = duplicateGroups.flatMap { it.duplicateCandidates },
            duplicateGroups = duplicateGroups,
            totalBytes = duplicateGroups.sumOf { it.totalReclaimableBytes },
            itemCount = duplicateGroups.sumOf { it.duplicateCandidates.size }
        )

        // 7. Group Blurry & Dark Photos (excluding items already tagged in duplicate clusters)
        val blurryItems = activeModels.filter {
            it.mediaId !in duplicateItemIds && it.category == MediaCategory.BLURRY
        }
        val blurryCategoryResult = ScanCategoryResult(
            category = MediaCategory.BLURRY,
            items = blurryItems,
            totalBytes = blurryItems.sumOf { it.fileSize },
            itemCount = blurryItems.size
        )

        // 8. Group WhatsApp Junk & Memes
        val whatsAppItems = activeModels.filter {
            it.mediaId !in duplicateItemIds && it.category == MediaCategory.WHATSAPP_JUNK
        }
        val whatsAppCategoryResult = ScanCategoryResult(
            category = MediaCategory.WHATSAPP_JUNK,
            items = whatsAppItems,
            totalBytes = whatsAppItems.sumOf { it.fileSize },
            itemCount = whatsAppItems.size
        )

        // 9. Group Old Screenshots (> 30 days) and Large Videos (> 50 MB)
        val screenshotItems = filterAnalyzer.filterOldScreenshots(rawMedia)
            .filter { !it.isIgnored && it.mediaId !in duplicateItemIds }
        val screenshotCategoryResult = ScanCategoryResult(
            category = MediaCategory.SCREENSHOT,
            items = screenshotItems,
            totalBytes = screenshotItems.sumOf { it.fileSize },
            itemCount = screenshotItems.size
        )

        val largeVideoItems = filterAnalyzer.filterLargeVideos(rawMedia)
            .filter { !it.isIgnored }
        val videoCategoryResult = ScanCategoryResult(
            category = MediaCategory.LARGE_VIDEO,
            items = largeVideoItems,
            totalBytes = largeVideoItems.sumOf { it.fileSize },
            itemCount = largeVideoItems.size
        )

        val resultMap = mapOf(
            MediaCategory.DUPLICATE to duplicateCategoryResult,
            MediaCategory.BLURRY to blurryCategoryResult,
            MediaCategory.WHATSAPP_JUNK to whatsAppCategoryResult,
            MediaCategory.SCREENSHOT to screenshotCategoryResult,
            MediaCategory.LARGE_VIDEO to videoCategoryResult
        )

        val finalTotalJunk = resultMap.values.sumOf { it.totalBytes }
        onProgress(
            ScanProgress(
                current = totalCount,
                total = totalCount,
                currentFileName = "Scan completed",
                junkBytesFound = finalTotalJunk,
                isScanning = false
            )
        )

        emit(resultMap)
    }.flowOn(Dispatchers.Default)

    private suspend fun analyzeNewItem(
        item: RawMediaItem,
        isDuplicateCandidate: Boolean,
        exactHashes: MutableMap<Long, String>
    ): MediaItemModel {
        if (item.isVideo) {
            val isLarge = filterAnalyzer.isLargeVideo(true, item.mimeType, item.size)
            return MediaItemModel(
                mediaId = item.id,
                uri = item.uri,
                displayName = item.displayName,
                bucketName = item.bucketName,
                fileSize = item.size,
                dateModified = item.dateModified,
                mimeType = item.mimeType,
                width = item.width,
                height = item.height,
                category = if (isLarge) MediaCategory.LARGE_VIDEO else MediaCategory.CLEAN,
                isSelected = false
            )
        }

        // Exact Duplicate SHA-256 Check if multiple files have the exact same byte length
        if (isDuplicateCandidate) {
            val sha256 = mediaStoreDataSource.computeSha256(item.uri)
            if (sha256 != null) {
                exactHashes[item.id] = sha256
            }
        }

        // Check if Old Screenshot
        if (filterAnalyzer.isOldScreenshot(item.bucketName, item.displayName, item.dateModified)) {
            return MediaItemModel(
                mediaId = item.id,
                uri = item.uri,
                displayName = item.displayName,
                bucketName = item.bucketName,
                fileSize = item.size,
                dateModified = item.dateModified,
                mimeType = item.mimeType,
                width = item.width,
                height = item.height,
                category = MediaCategory.SCREENSHOT,
                isSelected = true,
                similarityReason = "Screenshot older than 30 days"
            )
        }

        // Load 128x128 downscaled bitmap for pHash & Blur analysis (4x faster decode, 75% less memory)
        val analysisBitmap = mediaStoreDataSource.loadDownscaledBitmap(item.uri, 128, 128)
        if (analysisBitmap == null) {
            return MediaItemModel(
                mediaId = item.id,
                uri = item.uri,
                displayName = item.displayName,
                bucketName = item.bucketName,
                fileSize = item.size,
                dateModified = item.dateModified,
                mimeType = item.mimeType,
                width = item.width,
                height = item.height,
                category = MediaCategory.CLEAN
            )
        }

        val pHash = duplicateAnalyzer.computePHash(analysisBitmap)
        val blurResult = blurDetector.analyze(analysisBitmap)

        // WhatsApp Meme & Text Flyer Detection (run ML Kit only on WhatsApp media paths)
        val isWhatsApp = whatsAppClassifier.isWhatsAppPath(item.bucketName, item.uri.toString())
        var whatsAppJunkResult = false
        var whatsAppReason: String? = null

        if (isWhatsApp) {
            val classification = whatsAppClassifier.classify(analysisBitmap, item.bucketName, item.uri.toString())
            if (classification.isWhatsAppJunk) {
                whatsAppJunkResult = true
                whatsAppReason = classification.reason
            }
        }

        val category = when {
            whatsAppJunkResult -> MediaCategory.WHATSAPP_JUNK
            blurResult.isCandidateForCleaning -> MediaCategory.BLURRY
            else -> MediaCategory.CLEAN
        }

        return MediaItemModel(
            mediaId = item.id,
            uri = item.uri,
            displayName = item.displayName,
            bucketName = item.bucketName,
            fileSize = item.size,
            dateModified = item.dateModified,
            mimeType = item.mimeType,
            width = item.width,
            height = item.height,
            pHash = pHash,
            blurScore = blurResult.blurScore,
            category = category,
            isSelected = category != MediaCategory.CLEAN,
            similarityReason = whatsAppReason ?: blurResult.reason
        )
    }

    override fun prepareTrashOrDeleteRequest(
        mediaIds: List<Long>,
        isPermanent: Boolean
    ): PendingIntent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = mediaIds.map { id ->
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
            return if (isPermanent) {
                mediaStoreDataSource.createDeleteRequest(uris)
            } else {
                mediaStoreDataSource.createTrashRequest(uris, true)
            }
        }
        return null
    }

    override suspend fun executeLegacyDelete(mediaIds: List<Long>): Int {
        val uris = mediaIds.map { id ->
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        }
        return mediaStoreDataSource.deleteLegacy(uris)
    }

    override suspend fun onItemsDeleted(deletedIds: List<Long>, freedBytes: Long) =
        withContext(Dispatchers.IO) {
            database.scannedMediaDao().deleteByIds(deletedIds)
            val currentLifetime = prefs.getLong(KEY_LIFETIME_SAVED_BYTES, 0L)
            prefs.edit().putLong(KEY_LIFETIME_SAVED_BYTES, currentLifetime + freedBytes).apply()
        }

    private fun ScannedMediaEntity.toModel(): MediaItemModel {
        val resolvedCategory = try {
            MediaCategory.valueOf(category)
        } catch (_: Exception) {
            MediaCategory.CLEAN
        }

        return MediaItemModel(
            mediaId = mediaId,
            uri = Uri.parse(uri),
            displayName = displayName ?: "media_$mediaId",
            bucketName = bucketName,
            fileSize = fileSize,
            dateModified = dateModified,
            mimeType = mimeType ?: "image/jpeg",
            width = width,
            height = height,
            pHash = pHash,
            blurScore = blurScore,
            category = resolvedCategory,
            groupId = groupId,
            isBestShot = isBestShot,
            isIgnored = isIgnored,
            isSelected = !isBestShot && resolvedCategory != MediaCategory.CLEAN
        )
    }

    private fun MediaItemModel.toEntity(sha256: String? = null): ScannedMediaEntity {
        return ScannedMediaEntity(
            mediaId = mediaId,
            uri = uri.toString(),
            displayName = displayName,
            bucketName = bucketName,
            fileSize = fileSize,
            dateModified = dateModified,
            mimeType = mimeType,
            width = width,
            height = height,
            pHash = pHash,
            blurScore = blurScore,
            category = category.name,
            groupId = groupId,
            isBestShot = isBestShot,
            isIgnored = isIgnored,
            sha256 = sha256
        )
    }
}
