package com.example.smartcleanai.domain.analyzer

import android.graphics.Bitmap
import com.example.smartcleanai.domain.model.DuplicateGroup
import com.example.smartcleanai.domain.model.MediaCategory
import com.example.smartcleanai.domain.model.MediaItemModel
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class DuplicateAnalyzer {

    companion object {
        const val HAMMING_THRESHOLD = 5
        private const val PHASH_SIZE = 32
        private const val DCT_SIZE = 8
        private const val BURST_TIME_WINDOW_SECONDS = 86400L // 24 hours

        // Precomputed Cosine Table: cos((2x + 1) * u * PI / (2 * 32))
        // Statically initialized once for the lifetime of the process!
        private val COS_TABLE = DoubleArray(PHASH_SIZE * DCT_SIZE).apply {
            for (x in 0 until PHASH_SIZE) {
                for (u in 0 until DCT_SIZE) {
                    this[x * DCT_SIZE + u] = cos(((2.0 * x + 1.0) * u * PI) / (2.0 * PHASH_SIZE))
                }
            }
        }

        private val C_COEFF = DoubleArray(DCT_SIZE) { if (it == 0) 1.0 / sqrt(2.0) else 1.0 }
    }

    /**
     * Computes a 64-bit Perceptual Hash (pHash) using a Fast Separable 2D DCT
     * with precomputed cosine lookup tables on a 32x32 downscaled grayscale bitmap.
     * Runs in ~0.1ms without repeated JNI or runtime trigonometric calls.
     */
    fun computePHash(bitmap: Bitmap): Long {
        val scaled = if (bitmap.width != PHASH_SIZE || bitmap.height != PHASH_SIZE) {
            Bitmap.createScaledBitmap(bitmap, PHASH_SIZE, PHASH_SIZE, false)
        } else {
            bitmap
        }

        val pixels = IntArray(PHASH_SIZE * PHASH_SIZE)
        scaled.getPixels(pixels, 0, PHASH_SIZE, 0, 0, PHASH_SIZE, PHASH_SIZE)

        // 1. Grayscale luminance into 32x32 matrix using fast bit shifting
        val matrix = Array(PHASH_SIZE) { DoubleArray(PHASH_SIZE) }
        var idx = 0
        for (y in 0 until PHASH_SIZE) {
            val row = matrix[y]
            for (x in 0 until PHASH_SIZE) {
                val p = pixels[idx++]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                row[x] = 0.299 * r + 0.587 * g + 0.114 * b
            }
        }

        // 2. Fast Separable 2D DCT
        // Step A: 1D DCT on rows for first 8 frequencies (32 rows x 8 frequencies)
        val rowDct = Array(PHASH_SIZE) { DoubleArray(DCT_SIZE) }
        for (y in 0 until PHASH_SIZE) {
            val row = matrix[y]
            val rowOut = rowDct[y]
            for (u in 0 until DCT_SIZE) {
                var sum = 0.0
                for (x in 0 until PHASH_SIZE) {
                    sum += row[x] * COS_TABLE[x * DCT_SIZE + u]
                }
                rowOut[u] = sum
            }
        }

        // Step B: 1D DCT on columns for 8x8 low frequencies
        val dct = Array(DCT_SIZE) { DoubleArray(DCT_SIZE) }
        var sumTotal = 0.0
        var count = 0
        for (u in 0 until DCT_SIZE) {
            val cu = C_COEFF[u]
            for (v in 0 until DCT_SIZE) {
                val cv = C_COEFF[v]
                var sum = 0.0
                for (y in 0 until PHASH_SIZE) {
                    sum += rowDct[y][u] * COS_TABLE[y * DCT_SIZE + v]
                }
                val coeff = 0.25 * cu * cv * sum
                dct[u][v] = coeff
                if (u != 0 || v != 0) {
                    sumTotal += coeff
                    count++
                }
            }
        }

        val avg = sumTotal / count

        // Build 64-bit hash
        var hash = 0L
        var bitIndex = 0
        for (u in 0 until DCT_SIZE) {
            for (v in 0 until DCT_SIZE) {
                if (dct[u][v] > avg) {
                    hash = hash or (1L shl bitIndex)
                }
                bitIndex++
            }
        }

        return hash
    }

    /**
     * Alternative Difference Hash (dHash) on 9x8 grayscale bitmap.
     */
    fun computeDHash(bitmap: Bitmap): Long {
        val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, false)
        val pixels = IntArray(9 * 8)
        scaled.getPixels(pixels, 0, 9, 0, 0, 9, 8)

        var hash = 0L
        var bitIndex = 0

        for (y in 0 until 8) {
            val rowOffset = y * 9
            for (x in 0 until 8) {
                val left = pixels[rowOffset + x]
                val right = pixels[rowOffset + x + 1]

                val leftLum = ((left shr 16 and 0xFF) * 299 + (left shr 8 and 0xFF) * 587 + (left and 0xFF) * 114)
                val rightLum = ((right shr 16 and 0xFF) * 299 + (right shr 8 and 0xFF) * 587 + (right and 0xFF) * 114)

                if (leftLum > rightLum) {
                    hash = hash or (1L shl bitIndex)
                }
                bitIndex++
            }
        }
        return hash
    }

    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    /**
     * Groups photos by exact byte matches & SHA-256 hashes, as well as perceptual visual similarity
     * (Hamming distance <= 5).
     *
     * Identifies the "Best Shot" in each group (highest resolution and sharpness score),
     * leaving it unchecked, and pre-selecting duplicate candidates for deletion.
     */
    fun groupDuplicates(
        items: List<MediaItemModel>,
        exactHashes: Map<Long, String>
    ): List<DuplicateGroup> {
        val groups = mutableListOf<DuplicateGroup>()
        val visitedIds = mutableSetOf<Long>()

        // 1. Exact Duplicate clustering
        val sizeAndShaGroup = items.groupBy { item ->
            val sha = exactHashes[item.mediaId] ?: ""
            "${item.fileSize}_$sha"
        }

        for ((key, cluster) in sizeAndShaGroup) {
            if (key.endsWith("_") || cluster.size <= 1) continue

            val groupId = "exact_${UUID.randomUUID()}"
            val sortedByQuality = cluster.sortedWith(
                compareByDescending<MediaItemModel> { it.width * it.height }
                    .thenByDescending { it.blurScore ?: 0f }
                    .thenByDescending { it.dateModified }
            )

            val best = sortedByQuality.first().copy(
                isBestShot = true,
                isSelected = false,
                groupId = groupId,
                category = MediaCategory.DUPLICATE,
                similarityReason = "Exact File Match (SHA-256)"
            )
            val duplicates = sortedByQuality.drop(1).map {
                it.copy(
                    isBestShot = false,
                    isSelected = true,
                    groupId = groupId,
                    category = MediaCategory.DUPLICATE,
                    similarityReason = "Exact Duplicate (${it.formattedSize})"
                )
            }

            groups.add(DuplicateGroup(groupId, best, duplicates))
            visitedIds.addAll(cluster.map { it.mediaId })
        }

        // 2. Visually Similar (Burst) Photos clustering via pHash
        // Sort remaining items chronologically by dateModified to group burst shots efficiently
        val remaining = items.filter { it.mediaId !in visitedIds && it.pHash != null }
            .sortedByDescending { it.dateModified }

        val clustered = mutableListOf<MutableList<MediaItemModel>>()

        for (item in remaining) {
            val hash = item.pHash ?: continue
            var addedToGroup = false

            // Check against existing clusters (prioritizing recent clusters in the same bucket/timeframe)
            for (cluster in clustered) {
                val representative = cluster.first()
                // Fast filter: burst shots are typically in the same bucket and within 24 hours
                if (representative.bucketName == item.bucketName &&
                    abs(representative.dateModified - item.dateModified) <= BURST_TIME_WINDOW_SECONDS
                ) {
                    val repHash = representative.pHash ?: continue
                    if (hammingDistance(hash, repHash) <= HAMMING_THRESHOLD) {
                        cluster.add(item)
                        addedToGroup = true
                        break
                    }
                }
            }

            // Fallback: If not clustered by time window, check other clusters if total clusters is reasonable
            if (!addedToGroup && clustered.size < 50) {
                for (cluster in clustered) {
                    val repHash = cluster.first().pHash ?: continue
                    if (hammingDistance(hash, repHash) <= HAMMING_THRESHOLD) {
                        cluster.add(item)
                        addedToGroup = true
                        break
                    }
                }
            }

            if (!addedToGroup) {
                clustered.add(mutableListOf(item))
            }
        }

        for (cluster in clustered) {
            if (cluster.size <= 1) continue

            val groupId = "similar_${UUID.randomUUID()}"
            val sortedByQuality = cluster.sortedWith(
                compareByDescending<MediaItemModel> { it.width * it.height }
                    .thenByDescending { it.blurScore ?: 0f }
                    .thenByDescending { it.fileSize }
            )

            val best = sortedByQuality.first().copy(
                isBestShot = true,
                isSelected = false,
                groupId = groupId,
                category = MediaCategory.DUPLICATE,
                similarityReason = "Best Shot (Sharpest & Highest Res)"
            )

            val duplicates = sortedByQuality.drop(1).map {
                val dist = if (best.pHash != null && it.pHash != null) {
                    hammingDistance(best.pHash, it.pHash)
                } else 0
                it.copy(
                    isBestShot = false,
                    isSelected = true,
                    groupId = groupId,
                    category = MediaCategory.DUPLICATE,
                    similarityReason = "Visually Similar Burst (Distance $dist)"
                )
            }

            groups.add(DuplicateGroup(groupId, best, duplicates))
        }

        return groups
    }
}
