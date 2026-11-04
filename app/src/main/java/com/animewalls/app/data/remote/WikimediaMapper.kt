package com.animewalls.app.data.remote

import com.animewalls.app.data.remote.dto.CommonsPage
import com.animewalls.app.data.remote.dto.ImageInfoDto
import com.animewalls.app.domain.license.LicensePolicy
import com.animewalls.app.domain.model.Wallpaper

/**
 * Turns a raw Commons page into a [Wallpaper], or returns null when the image
 * fails any compliance check. Every rejection is deliberate.
 */
class WikimediaMapper {

    fun map(page: CommonsPage, query: String): Wallpaper? {
        val info = page.imageInfo?.firstOrNull() ?: return null
        val meta = info.extMetadata.orEmpty()

        val description = LicensePolicy.stripHtml(meta["ImageDescription"]?.text())
        val title = page.title.removePrefix("File:")
        val commonsCategories = LicensePolicy.stripHtml(meta["Categories"]?.text())

        // 1. Rights: the licence must permit redistribution.
        val licenseShort = meta["LicenseShortName"]?.text().orEmpty()
        val licenseCode = meta["License"]?.text().orEmpty()
        val usageTerms = meta["UsageTerms"]?.text().orEmpty()
        if (!LicensePolicy.isAllowedLicense(licenseShort, licenseCode, usageTerms)) return null

        // 2. Rights: the *content* must not be a copyrighted character or a scan.
        // Categories catch files filed under a franchise without naming it in the title.
        if (LicensePolicy.rejectionReason(title, "$description $commonsCategories") != null) return null

        // 3. Relevance: the image must actually be about what was searched.
        val haystack = buildString {
            append(title).append(' ').append(description)
            append(' ').append(meta["ObjectName"]?.text().orEmpty())
            append(' ').append(meta["Categories"]?.text().orEmpty())
        }
        if (!LicensePolicy.isRelevant(query, haystack)) return null

        // 4. It has to be usable as a wallpaper.
        if (!LicensePolicy.isWallpaperQuality(info.width, info.height)) return null
        if (!isSupportedMime(info.mime)) return null

        val fullUrl = info.url ?: return null
        val thumb = info.thumbUrl ?: fullUrl
        val sourceUrl = info.descriptionUrl ?: return null

        return Wallpaper(
            id = info.sha1?.takeIf { it.isNotBlank() } ?: "page-${page.pageId}",
            pageId = page.pageId,
            title = title.substringBeforeLast('.').replace('_', ' ').trim(),
            description = description,
            thumbUrl = thumb,
            fullUrl = fullUrl,
            width = info.width,
            height = info.height,
            mime = info.mime.orEmpty(),
            artist = LicensePolicy.stripHtml(meta["Artist"]?.text()).ifBlank {
                LicensePolicy.stripHtml(meta["Credit"]?.text()).ifBlank { "Unknown author" }
            },
            credit = LicensePolicy.stripHtml(meta["Credit"]?.text()),
            licenseShortName = licenseShort.ifBlank { licenseCode },
            licenseUrl = meta["LicenseUrl"]?.text().orEmpty(),
            licenseName = usageTerms.ifBlank { licenseShort },
            sourceUrl = sourceUrl,
            attribution = LicensePolicy.stripHtml(meta["Attribution"]?.text()),
            dateCreated = LicensePolicy.stripHtml(meta["DateTimeOriginal"]?.text()),
            categories = LicensePolicy.stripHtml(meta["Categories"]?.text()),
        )
    }

    private fun isSupportedMime(mime: String?): Boolean {
        val m = mime?.lowercase() ?: return false
        return m == "image/jpeg" || m == "image/png" || m == "image/webp"
    }
}
