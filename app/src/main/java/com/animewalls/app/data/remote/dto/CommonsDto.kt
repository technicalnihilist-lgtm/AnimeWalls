package com.animewalls.app.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class CommonsResponse(
    @SerializedName("query") val query: QueryBlock? = null,
    @SerializedName("continue") val continuation: ContinueBlock? = null,
    @SerializedName("error") val error: CommonsError? = null,
)

data class QueryBlock(
    @SerializedName("pages") val pages: List<CommonsPage>? = null,
)

data class ContinueBlock(
    @SerializedName("gsroffset") val gsrOffset: Int? = null,
    @SerializedName("continue") val token: String? = null,
)

data class CommonsError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("info") val info: String? = null,
)

data class CommonsPage(
    @SerializedName("pageid") val pageId: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("imageinfo") val imageInfo: List<ImageInfoDto>? = null,
)

data class ImageInfoDto(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("sha1") val sha1: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, MetaValue>? = null,
)

data class MetaValue(
    @SerializedName("value") val value: JsonElement? = null,
) {
    fun text(): String {
        val element = value ?: return ""
        return try {
            if (element.isJsonPrimitive) element.asString else ""
        } catch (t: Throwable) {
            ""
        }
    }
}
