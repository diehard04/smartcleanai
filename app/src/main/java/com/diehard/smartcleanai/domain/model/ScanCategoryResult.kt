package com.diehard.smartcleanai.domain.model

data class DuplicateGroup(
    val groupId: String,
    val bestShot: MediaItemModel,
    val duplicateCandidates: List<MediaItemModel>
) {
    val totalReclaimableBytes: Long
        get() = duplicateCandidates.filter { it.isSelected }.sumOf { it.fileSize }
}

data class ScanCategoryResult(
    val category: MediaCategory,
    val items: List<MediaItemModel> = emptyList(),
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val totalBytes: Long = 0L,
    val itemCount: Int = 0
) {
    val formattedTotalBytes: String
        get() {
            val mb = totalBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                else -> String.format("%.1f MB", mb)
            }
        }
}
