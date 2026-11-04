package com.animewalls.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.animewalls.app.domain.model.Wallpaper

/** Flat, reusable column set shared by favourites, downloads and history. */
data class WallpaperRecord(
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
) {
    fun toDomain(id: String): Wallpaper = Wallpaper(
        id = id, pageId = pageId, title = title, description = description,
        thumbUrl = thumbUrl, fullUrl = fullUrl, width = width, height = height,
        mime = mime, artist = artist, credit = credit,
        licenseShortName = licenseShortName, licenseUrl = licenseUrl,
        licenseName = licenseName, sourceUrl = sourceUrl, attribution = attribution,
        dateCreated = dateCreated, categories = categories,
    )

    companion object {
        fun from(w: Wallpaper): WallpaperRecord = WallpaperRecord(
            pageId = w.pageId, title = w.title, description = w.description,
            thumbUrl = w.thumbUrl, fullUrl = w.fullUrl, width = w.width, height = w.height,
            mime = w.mime, artist = w.artist, credit = w.credit,
            licenseShortName = w.licenseShortName, licenseUrl = w.licenseUrl,
            licenseName = w.licenseName, sourceUrl = w.sourceUrl,
            attribution = w.attribution, dateCreated = w.dateCreated,
            categories = w.categories,
        )
    }
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    @Embedded val record: WallpaperRecord,
    val savedAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(): Wallpaper = record.toDomain(id)
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    @Embedded val record: WallpaperRecord,
    val localPath: String,
    val downloadedAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(): Wallpaper = record.toDomain(id)
}

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val id: String,
    @Embedded val record: WallpaperRecord,
    val viewedAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(): Wallpaper = record.toDomain(id)
}
