package com.example.smartcleanai.domain.analyzer

import android.graphics.Bitmap

data class BlurAnalysisResult(
    val blurScore: Float,
    val isBlurry: Boolean,
    val averageLuminance: Float,
    val isDarkPocketShot: Boolean,
    val isCandidateForCleaning: Boolean,
    val reason: String
)

class BlurDetector(
    private var blurThreshold: Float = DEFAULT_MEDIUM_THRESHOLD
) {

    companion object {
        const val LOW_SENSITIVITY_THRESHOLD = 70.0f
        const val DEFAULT_MEDIUM_THRESHOLD = 100.0f
        const val HIGH_SENSITIVITY_THRESHOLD = 150.0f
        const val DARK_POCKET_LUMINANCE_THRESHOLD = 15.0f
        private const val ANALYSIS_WIDTH = 128
        private const val ANALYSIS_HEIGHT = 128
    }

    fun setSensitivityThreshold(threshold: Float) {
        this.blurThreshold = threshold
    }

    /**
     * Calculates the Laplacian Variance and average pixel luminance on a 128x128 downscaled bitmap.
     * Uses optimized float primitive buffers and fast bit-shifting for sub-millisecond execution.
     */
    fun analyze(bitmap: Bitmap): BlurAnalysisResult {
        val scaled = if (bitmap.width != ANALYSIS_WIDTH || bitmap.height != ANALYSIS_HEIGHT) {
            Bitmap.createScaledBitmap(bitmap, ANALYSIS_WIDTH, ANALYSIS_HEIGHT, false)
        } else {
            bitmap
        }

        val totalPixels = ANALYSIS_WIDTH * ANALYSIS_HEIGHT
        val pixels = IntArray(totalPixels)
        scaled.getPixels(pixels, 0, ANALYSIS_WIDTH, 0, 0, ANALYSIS_WIDTH, ANALYSIS_HEIGHT)

        val grayscale = FloatArray(totalPixels)
        var totalLuminance = 0.0

        for (i in 0 until totalPixels) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = (0.299f * r) + (0.587f * g) + (0.114f * b)
            grayscale[i] = lum
            totalLuminance += lum
        }

        val avgLuminance = (totalLuminance / totalPixels).toFloat()
        val isDarkShot = avgLuminance < DARK_POCKET_LUMINANCE_THRESHOLD

        // Convolve with 3x3 Laplacian kernel
        var sumLap = 0.0
        var sumLapSq = 0.0
        var sampleCount = 0

        val width = ANALYSIS_WIDTH
        val height = ANALYSIS_HEIGHT

        for (y in 1 until height - 1) {
            val yOffset = y * width
            val yPrevOffset = (y - 1) * width
            val yNextOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val center = grayscale[yOffset + x]
                val top = grayscale[yPrevOffset + x]
                val bottom = grayscale[yNextOffset + x]
                val left = grayscale[yOffset + x - 1]
                val right = grayscale[yOffset + x + 1]

                val laplacian = top + bottom + left + right - (4.0f * center)
                sumLap += laplacian
                sumLapSq += laplacian * laplacian
                sampleCount++
            }
        }

        val mean = sumLap / sampleCount
        val variance = ((sumLapSq / sampleCount) - (mean * mean)).toFloat().coerceAtLeast(0f)

        val isBlurry = variance < blurThreshold
        val isJunk = isDarkShot || isBlurry

        val reason = when {
            isDarkShot -> "Accidental Dark/Pocket Shot (Avg Lum: ${"%.1f".format(avgLuminance)})"
            isBlurry -> "Motion / Focus Blur (Score: ${"%.1f".format(variance)} < ${"%.1f".format(blurThreshold)})"
            else -> "Sharp & Well Lit (Score: ${"%.1f".format(variance)})"
        }

        return BlurAnalysisResult(
            blurScore = variance,
            isBlurry = isBlurry,
            averageLuminance = avgLuminance,
            isDarkPocketShot = isDarkShot,
            isCandidateForCleaning = isJunk,
            reason = reason
        )
    }
}
