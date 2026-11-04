package com.animewalls.app.domain.model

enum class CategoryGroup(val label: String) {
    POPULAR_ANIME("Popular Anime"),
    CHARACTERS("Anime Characters"),
    SCENERY("Anime Scenery"),
    COLLECTIONS("Special Collections"),
}

data class WallpaperCategory(
    val id: String,
    val name: String,
    val group: CategoryGroup,
    val queries: List<String>,
    val blurb: String,
)
