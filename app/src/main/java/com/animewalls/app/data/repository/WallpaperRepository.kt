package com.animewalls.app.data.repository

import com.animewalls.app.data.local.AnimeWallsDatabase
import com.animewalls.app.data.local.entity.DownloadEntity
import com.animewalls.app.data.local.entity.FavoriteEntity
import com.animewalls.app.data.local.entity.RecentEntity
import com.animewalls.app.data.remote.WikimediaApi
import com.animewalls.app.data.remote.WikimediaMapper
import com.animewalls.app.domain.catalog.CategoryCatalog
import com.animewalls.app.domain.model.PageResult
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Live data source. Nothing here is pre-baked: every wallpaper shown in the app
 * is fetched from the Wikimedia Commons API at request time, filtered through
 * [com.animewalls.app.domain.license.LicensePolicy], and cached only as image
 * bytes by Coil.
 */
class WallpaperRepository(
    private val api: WikimediaApi,
    private val mapper: WikimediaMapper,
    private val database: AnimeWallsDatabase,
) {

    private val sessionCache = java.util.concurrent.ConcurrentHashMap<String, Wallpaper>()

    /** Keeps the full record reachable by id for the detail screen. */
    fun remember(wallpaper: Wallpaper) { sessionCache[wallpaper.id] = wallpaper }

    fun cached(id: String): Wallpaper? = sessionCache[id]

    suspend fun findById(id: String): Wallpaper? {
        sessionCache[id]?.let { return it }
        database.downloadDao().findById(id)?.let { return it.toDomain() }
        return database.favoriteDao().observeAll().first().firstOrNull { it.id == id }?.toDomain()
    }

    companion object {
        /** Raw pages fetched per request; filtered results are usually a subset. */
        private const val RAW_BATCH = 25
        private const val MAX_RAW_PAGES_PER_CALL = 4
    }

    // ---------------------------------------------------------------- remote

    /**
     * Loads one filtered page for [query]. Because the policy filter removes
     * most raw hits for some queries, we keep pulling raw pages until the
     * requested number of usable items is reached (bounded, to keep latency
     * predictable).
     */
    suspend fun loadPage(query: String, offset: Int, limit: Int, maxRawPages: Int = MAX_RAW_PAGES_PER_CALL): PageResult {
        val collected = LinkedHashMap<String, Wallpaper>()
        var rawOffset = offset
        var hasMore = false
        var attempts = 0

        try {
            while (collected.size < limit && attempts < maxRawPages) {
                attempts++
                val response = withRetry {
                    api.searchImages(
                        search = "$query filetype:bitmap",
                        limit = RAW_BATCH,
                        offset = if (rawOffset == 0) null else rawOffset,
                    )
                }
                response.error?.info?.let { return PageResult.failure(it) }

                val pages = response.query?.pages.orEmpty()
                if (pages.isEmpty()) { hasMore = false; break }

                for (page in pages) {
                    val wallpaper = mapper.map(page, query) ?: continue
                    remember(wallpaper)
                    collected.putIfAbsent(wallpaper.id, wallpaper)
                }

                val next = response.continuation?.gsrOffset
                if (next == null || next <= rawOffset) { hasMore = false; break }
                rawOffset = next
                hasMore = true
            }
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            if (collected.isEmpty()) return PageResult.failure(friendly(t))
        }

        return PageResult(collected.values.toList().take(limit), rawOffset, hasMore)
    }

    suspend fun loadCategory(categoryId: String, offset: Int, limit: Int): PageResult {
        val category = CategoryCatalog.byId(categoryId)
            ?: return PageResult.failure("Unknown category")
        val primary = category.queries.first()

        // First page: merge every query the category declares, so a collection is
        // rich immediately. Later pages: continue paging the primary query only.
        if (offset == 0 && category.queries.size > 1) {
            val merged = LinkedHashMap<String, Wallpaper>()
            var anyMore = false
            var error: String? = null
            for (query in category.queries) {
                val page = loadPage(query, 0, limit, maxRawPages = 1)
                page.items.forEach { merged.putIfAbsent(it.id, it) }
                anyMore = anyMore || page.hasMore
                if (page.error != null) error = page.error
            }
            return PageResult(merged.values.toList().take(limit), 0, anyMore, error)
        }

        val result = loadPage(primary, offset, limit)
        if (result.items.isNotEmpty() || category.queries.size == 1) return result

        for (alternative in category.queries.drop(1)) {
            val alt = loadPage(alternative, 0, limit)
            if (alt.items.isNotEmpty()) return alt
        }
        return result
    }

    /** A rail of wallpapers drawn from several queries, de-duplicated. */
    suspend fun loadRail(queries: List<String>, perQuery: Int = 8): List<Wallpaper> {
        val out = LinkedHashMap<String, Wallpaper>()
        for (query in queries) {
            if (out.size >= perQuery * 2) break
            val page = loadPage(query, 0, perQuery, maxRawPages = 1)
            for (item in page.items) out.putIfAbsent(item.id, item)
        }
        return out.values.toList()
    }

    /** Deterministic "wallpaper of the day" that changes daily. */
    suspend fun dailyWallpaper(): Wallpaper? {
        val day = System.currentTimeMillis() / 86_400_000L
        val queries = CategoryCatalog.featuredQueries
        val query = queries[(day % queries.size).toInt()]
        val page = loadPage(query, 0, 12)
        if (page.items.isEmpty()) return null
        return page.items[(day % page.items.size).toInt()]
    }

    suspend fun search(query: String, offset: Int, limit: Int): PageResult {
        if (query.isBlank()) return PageResult.empty()
        return loadPage(query.trim(), offset, limit)
    }

    // ----------------------------------------------------------------- local

    fun observeFavorites(): Flow<List<Wallpaper>> =
        database.favoriteDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun observeDownloads(): Flow<List<Wallpaper>> =
        database.downloadDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun observeRecent(limit: Int = 24): Flow<List<Wallpaper>> =
        database.recentDao().observeRecent(limit).map { list -> list.map { it.toDomain() } }

    fun observeIsFavorite(id: String): Flow<Boolean> = database.favoriteDao().observeIsFavorite(id)

    fun observeFavoriteCount(): Flow<Int> = database.favoriteDao().observeCount()

    fun observeDownloadCount(): Flow<Int> = database.downloadDao().observeCount()

    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean {
        val dao = database.favoriteDao()
        return if (dao.isFavorite(wallpaper.id)) {
            dao.delete(wallpaper.id); false
        } else {
            dao.insert(FavoriteEntity(wallpaper.id, WallpaperRecordMapper.from(wallpaper))); true
        }
    }

    suspend fun addFavorite(wallpaper: Wallpaper) {
        database.favoriteDao().insert(FavoriteEntity(wallpaper.id, WallpaperRecordMapper.from(wallpaper)))
    }

    suspend fun recordView(wallpaper: Wallpaper) {
        database.recentDao().insert(RecentEntity(wallpaper.id, WallpaperRecordMapper.from(wallpaper)))
    }

    suspend fun downloadedFile(id: String): String? =
        database.downloadDao().findById(id)?.localPath

    suspend fun registerDownload(wallpaper: Wallpaper, path: String) {
        database.downloadDao().insert(
            DownloadEntity(wallpaper.id, WallpaperRecordMapper.from(wallpaper), path)
        )
    }

    suspend fun removeDownload(id: String) = database.downloadDao().delete(id)

    /** Everything the Credits screen must display, from local + live data. */
    suspend fun creditSources(): List<Wallpaper> {
        val map = LinkedHashMap<String, Wallpaper>()
        database.favoriteDao().observeAll().first().forEach { map.putIfAbsent(it.id, it.toDomain()) }
        database.downloadDao().observeAll().first().forEach { map.putIfAbsent(it.id, it.toDomain()) }
        database.recentDao().observeRecent(60).first().forEach { map.putIfAbsent(it.id, it.toDomain()) }
        return map.values.sortedBy { it.title.lowercase() }
    }

    // ---------------------------------------------------------------- helper

    private suspend fun <T> withRetry(times: Int = 3, block: suspend () -> T): T {
        var lastError: Throwable? = null
        for (attempt in 0 until times) {
            try {
                return block()
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                lastError = t
                if (attempt < times - 1) delay(500L * (attempt + 1))
            }
        }
        throw lastError ?: IOException("Request failed")
    }

    private fun friendly(t: Throwable): String = when (t) {
        is IOException -> "No connection. AnimeWalls needs the internet to load live wallpapers."
        else -> t.message ?: "Something went wrong while contacting Wikimedia Commons."
    }
}

private object WallpaperRecordMapper {
    fun from(w: Wallpaper) = com.animewalls.app.data.local.entity.WallpaperRecord.from(w)
}
