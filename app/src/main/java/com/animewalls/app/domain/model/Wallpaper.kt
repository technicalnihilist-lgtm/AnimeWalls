package com.animewalls.app.domain.model

/**
 * A single wallpaper backed by a Wikimedia Commons file.
 * Every field that appears in the Credits screen is copied verbatim from the
 * Commons `imageinfo` / `extmetadata` payload -- nothing is invented.
 */
data class Wallpaper(
    val id: String,
    val pageId: Long,
    val title: String,
    val description: String,
    val thumbUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val mime: String,
    val artist: String,
    val credit: String,
    val licenseShortName: String,
    val licenseUrl: String,
    val licenseName: String,
    val sourceUrl: String,
    val attribution: String,
    val dateCreated: String,
    val categories: String,
    val fetchedAt: Long = System.currentTimeMillis(),
) {
    val resolution: String get() = "$width × $height"
    val isPortrait: Boolean get() = height >= width
    val aspectRatio: Float get() = if (height == 0) 1f else width.toFloat() / height
    val isAmoledFriendly: Boolean get() = true
}

data class PageResult(
    val items: List<Wallpaper>,
    val nextOffset: Int,
    val hasMore: Boolean,
    val error: String? = null,
) {
    val isFailure: Boolean get() = error != null && items.isEmpty()
    companion object {
        fun empty() = PageResult(emptyList(), 0, false, null)
        fun failure(message: String) = PageResult(emptyList(), 0, false, message)
    }
}
