package com.animewalls.app.data.remote

import com.animewalls.app.data.remote.dto.CommonsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikimediaApi {

    @GET("w/api.php")
    suspend fun searchImages(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2,
        @Query("generator") generator: String = "search",
        @Query("gsrsearch") search: String,
        @Query("gsrnamespace") namespace: Int = 6,
        @Query("gsrlimit") limit: Int,
        @Query("gsroffset") offset: Int? = null,
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") infoProps: String = "url|size|mime|sha1|extmetadata",
        @Query("iiurlwidth") thumbWidth: Int = 900,
    ): CommonsResponse
}
