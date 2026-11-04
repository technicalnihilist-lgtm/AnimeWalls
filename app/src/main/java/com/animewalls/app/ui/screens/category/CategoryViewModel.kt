package com.animewalls.app.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animewalls.app.di.ServiceLocator
import com.animewalls.app.domain.catalog.CategoryCatalog
import com.animewalls.app.domain.model.CategoryGroup
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.domain.model.WallpaperCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val items: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val loadedOnce: Boolean = false,
)

class CategoryViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ServiceLocator.repository
    val categoryId: String = savedStateHandle.get<String>("categoryId").orEmpty()
    val category: WallpaperCategory? = CategoryCatalog.byId(categoryId)

    private val _state = MutableStateFlow(CategoryUiState())
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavorites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var offset = 0
    private val seen = LinkedHashSet<String>()

    init { load(reset = true) }

    fun load(reset: Boolean = false) {
        if (_state.value.isLoading || _state.value.isLoadingMore) return
        if (!reset && !_state.value.hasMore) return
        viewModelScope.launch {
            if (reset) {
                offset = 0; seen.clear()
                _state.value = _state.value.copy(isLoading = true, error = null, items = emptyList())
            } else {
                _state.value = _state.value.copy(isLoadingMore = true)
            }

            val page = repository.loadCategory(categoryId, offset, PAGE_SIZE)
            val fresh = page.items.filter { seen.add(it.id) }
            offset = page.nextOffset
            _state.value = _state.value.copy(
                items = if (reset) fresh else _state.value.items + fresh,
                isLoading = false,
                isLoadingMore = false,
                hasMore = page.hasMore && fresh.isNotEmpty(),
                error = page.error,
                loadedOnce = true,
            )
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }

    /**
     * Explains an empty result honestly, depending on which kind of category it is.
     */
    fun emptyTitle(): String = when (category?.group) {
        CategoryGroup.POPULAR_ANIME, CategoryGroup.CHARACTERS -> "No verifiable artwork"
        else -> "Nothing here yet"
    }

    fun emptyMessage(): String = when (category?.group) {
        CategoryGroup.POPULAR_ANIME, CategoryGroup.CHARACTERS ->
            "Wikimedia Commons has no image of ${category?.name} that AnimeWalls can confirm is " +
                "freely licensed. Screenshots, fan art and cosplay photographs all depict " +
                "copyrighted characters, so they are withheld rather than shown without rights."
        else ->
            "The live search for this collection returned nothing that passed the licence and " +
                "quality checks. Try again in a moment — the feed updates continuously."
    }

    companion object { private const val PAGE_SIZE = 24 }
}
