package com.diehard.smartcleanai.domain.model

data class ScanProgress(
    val current: Int = 0,
    val total: Int = 0,
    val currentFileName: String = "",
    val junkBytesFound: Long = 0L,
    val isScanning: Boolean = false
) {
    val progressFraction: Float
        get() = if (total > 0) current.toFloat() / total.toFloat() else 0f

    val formattedJunkFound: String
        get() {
            val mb = junkBytesFound / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                else -> String.format("%.1f MB", mb)
            }
        }
}
