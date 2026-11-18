package com.example.smartcleanai.domain.analyzer

import android.net.Uri
import com.example.smartcleanai.domain.model.MediaCategory
import com.example.smartcleanai.domain.model.MediaItemModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class AnalyzerTest {

    private val duplicateAnalyzer = DuplicateAnalyzer()
    private val filterAnalyzer = MediaFilterAnalyzer()
    private val blurDetector = BlurDetector()

    @Test
    fun testHammingDistance() {
        val hash1 = 0b10101010L
        val hash2 = 0b10101011L // 1 bit flipped
        assertEquals(1, duplicateAnalyzer.hammingDistance(hash1, hash2))

        val hash3 = 0b11111111L
        val hash4 = 0b00000000L // 8 bits flipped
        assertEquals(8, duplicateAnalyzer.hammingDistance(hash3, hash4))

        val hash5 = 0b10101010L
        assertEquals(0, duplicateAnalyzer.hammingDistance(hash1, hash5))
    }

    @Test
    fun testExactDuplicateClustering() {
        val dummyUri = Uri.EMPTY ?: Uri.parse("")
        val item1 = MediaItemModel(
            mediaId = 1L,
            uri = dummyUri,
            displayName = "IMG_001.JPG",
            bucketName = "Camera",
            fileSize = 4096000L,
            dateModified = 1000L,
            mimeType = "image/jpeg",
            width = 4000,
            height = 3000,
            blurScore = 120f
        )
        val item2 = MediaItemModel(
            mediaId = 2L,
            uri = dummyUri,
            displayName = "IMG_001_copy.JPG",
            bucketName = "Camera",
            fileSize = 4096000L,
            dateModified = 1010L,
            mimeType = "image/jpeg",
            width = 4000,
            height = 3000,
            blurScore = 120f
        )

        val exactHashes = mapOf(1L to "abcdef123456", 2L to "abcdef123456")
        val groups = duplicateAnalyzer.groupDuplicates(listOf(item1, item2), exactHashes)

        assertEquals(1, groups.size)
        val group = groups[0]
        assertTrue(group.bestShot.isBestShot)
        assertFalse(group.bestShot.isSelected)
        assertEquals(1, group.duplicateCandidates.size)
        assertTrue(group.duplicateCandidates[0].isSelected)
        assertFalse(group.duplicateCandidates[0].isBestShot)
    }

    @Test
    fun testBurstSimilarClusteringWithinHammingThreshold() {
        val dummyUri = Uri.EMPTY ?: Uri.parse("")
        val baseHash = 0x123456789ABCDEF0L
        val burstHash = baseHash xor 0x3L // 2 bits flipped (<= 5)

        val item1 = MediaItemModel(
            mediaId = 10L,
            uri = dummyUri,
            displayName = "BURST_001.JPG",
            bucketName = "Camera",
            fileSize = 3000000L,
            dateModified = 1000L,
            mimeType = "image/jpeg",
            width = 4000,
            height = 3000,
            pHash = baseHash,
            blurScore = 180f
        )
        val item2 = MediaItemModel(
            mediaId = 11L,
            uri = dummyUri,
            displayName = "BURST_002.JPG",
            bucketName = "Camera",
            fileSize = 2950000L,
            dateModified = 1001L,
            mimeType = "image/jpeg",
            width = 4000,
            height = 3000,
            pHash = burstHash,
            blurScore = 110f // lower sharpness
        )

        val groups = duplicateAnalyzer.groupDuplicates(listOf(item1, item2), emptyMap())
        assertEquals(1, groups.size)
        val group = groups[0]
        assertEquals(10L, group.bestShot.mediaId) // sharpest photo tagged Best Shot
        assertTrue(group.bestShot.isBestShot)
        assertEquals(11L, group.duplicateCandidates[0].mediaId)
        assertTrue(group.duplicateCandidates[0].isSelected)
    }

    @Test
    fun testOldScreenshotDetection() {
        val nowSeconds = System.currentTimeMillis() / 1000L
        val fortyDaysAgoSeconds = nowSeconds - TimeUnit.DAYS.toSeconds(40)
        val fiveDaysAgoSeconds = nowSeconds - TimeUnit.DAYS.toSeconds(5)

        // 40 days old screenshot
        assertTrue(
            filterAnalyzer.isOldScreenshot("Screenshots", "Screenshot_20240101.png", fortyDaysAgoSeconds)
        )

        // 5 days old screenshot (should NOT be flagged)
        assertFalse(
            filterAnalyzer.isOldScreenshot("Screenshots", "Screenshot_20240215.png", fiveDaysAgoSeconds)
        )

        // Normal camera photo from 40 days ago
        assertFalse(
            filterAnalyzer.isOldScreenshot("Camera", "IMG_20240101.jpg", fortyDaysAgoSeconds)
        )
    }

    @Test
    fun testLargeVideoDetection() {
        val sixtyMbBytes = 60L * 1024 * 1024
        val thirtyMbBytes = 30L * 1024 * 1024

        assertTrue(filterAnalyzer.isLargeVideo(isVideo = true, mimeType = "video/mp4", sizeBytes = sixtyMbBytes))
        assertFalse(filterAnalyzer.isLargeVideo(isVideo = true, mimeType = "video/mp4", sizeBytes = thirtyMbBytes))
        assertFalse(filterAnalyzer.isLargeVideo(isVideo = false, mimeType = "image/jpeg", sizeBytes = sixtyMbBytes))
    }

    @Test
    fun testBlurSensitivityThresholds() {
        blurDetector.setSensitivityThreshold(BlurDetector.HIGH_SENSITIVITY_THRESHOLD)
        assertEquals(150.0f, BlurDetector.HIGH_SENSITIVITY_THRESHOLD, 0.01f)
        assertEquals(100.0f, BlurDetector.DEFAULT_MEDIUM_THRESHOLD, 0.01f)
        assertEquals(70.0f, BlurDetector.LOW_SENSITIVITY_THRESHOLD, 0.01f)
    }
}
