package com.example.smartcleanai.domain.analyzer

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class WhatsAppClassificationResult(
    val isWhatsAppJunk: Boolean,
    val isWhatsAppSource: Boolean,
    val wordCount: Int,
    val faceCount: Int,
    val textCoverageRatio: Float,
    val detectedSnippet: String,
    val reason: String
)

class WhatsAppJunkClassifier {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
        FaceDetection.getClient(options)
    }

    private val greetingKeywords = listOf(
        "good morning", "good night", "suprabhat", "subhodhayam", "shubh",
        "radhe", "ram ram", "blessed", "quote", "motivat", "forwarded",
        "sale", "discount", "offer", "congratulat", "happy birthday", "happy new year",
        "festival", "diwali", "eid", "christmas", "thought for the day"
    )

    fun isWhatsAppPath(bucketName: String, pathUri: String): Boolean {
        val lowerBucket = bucketName.lowercase(Locale.ROOT)
        val lowerUri = pathUri.lowercase(Locale.ROOT)
        return lowerBucket.contains("whatsapp") ||
                lowerUri.contains("com.whatsapp") ||
                lowerUri.contains("whatsapp images") ||
                lowerUri.contains("whatsapp/media")
    }

    suspend fun classify(
        bitmap: Bitmap,
        bucketName: String,
        uriString: String
    ): WhatsAppClassificationResult = withContext(Dispatchers.Default) {
        val isSourceWhatsApp = isWhatsAppPath(bucketName, uriString)

        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // 1. Run ML Kit Text Recognition
            val visionTextTask = textRecognizer.process(inputImage)
            val visionText = Tasks.await(visionTextTask)

            val fullText = visionText.text.trim()
            val words = fullText.split("\\s+".toRegex()).filter { it.isNotBlank() }
            val wordCount = words.size

            // Compute approximate text coverage ratio
            val totalBitmapArea = (bitmap.width * bitmap.height).coerceAtLeast(1)
            var textBoundingArea = 0L
            for (block in visionText.textBlocks) {
                val box: Rect? = block.boundingBox
                if (box != null) {
                    textBoundingArea += (box.width().toLong() * box.height().toLong()).coerceAtLeast(0L)
                }
            }
            val textCoverageRatio = (textBoundingArea.toFloat() / totalBitmapArea.toFloat()).coerceIn(0f, 1f)

            // Check for greeting / meme keywords
            val lowerText = fullText.lowercase(Locale.ROOT)
            val containsGreeting = greetingKeywords.any { lowerText.contains(it) }

            // 2. Run ML Kit Face Detection
            val faceTask = faceDetector.process(inputImage)
            val faces = Tasks.await(faceTask)
            val faceCount = faces.size

            // Criteria:
            // - If source is WhatsApp: text is heavy (word count >= 8 or text coverage > 8% or contains greetings) AND 0 faces
            // - Even if non-WhatsApp: heavy greeting/quote text (word count >= 16 or text coverage > 15%) AND 0 faces
            val isHeavyText = (wordCount >= 8 || textCoverageRatio >= 0.08f || containsGreeting)
            val isJunk = if (isSourceWhatsApp) {
                isHeavyText && faceCount == 0
            } else {
                (wordCount >= 18 || (containsGreeting && wordCount >= 5)) && faceCount == 0
            }

            val snippet = if (fullText.length > 50) fullText.take(50) + "..." else fullText

            val reason = when {
                isJunk && isSourceWhatsApp -> "WhatsApp Meme / Forwarded Flyer ($wordCount words, 0 faces)"
                isJunk -> "Text-Heavy Flyer / Meme ($wordCount words, 0 faces)"
                faceCount > 0 -> "Contains Human Face ($faceCount face(s) preserved)"
                else -> "Clean Personal Photo"
            }

            WhatsAppClassificationResult(
                isWhatsAppJunk = isJunk,
                isWhatsAppSource = isSourceWhatsApp,
                wordCount = wordCount,
                faceCount = faceCount,
                textCoverageRatio = textCoverageRatio,
                detectedSnippet = snippet,
                reason = reason
            )
        } catch (e: Exception) {
            // Graceful fallback if ML Kit is not ready
            WhatsAppClassificationResult(
                isWhatsAppJunk = false,
                isWhatsAppSource = isSourceWhatsApp,
                wordCount = 0,
                faceCount = 0,
                textCoverageRatio = 0f,
                detectedSnippet = "",
                reason = "Classification skipped (${e.localizedMessage ?: "error"})"
            )
        }
    }
}
