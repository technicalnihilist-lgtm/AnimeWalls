package com.animewalls.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animewalls.app.di.ServiceLocator
import com.animewalls.app.domain.catalog.CategoryCatalog
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val daily: Wallpaper? = null,
    val featured: List<Wallpaper> = emptyList(),
    val trending: List<Wallpaper> = emptyList(),
    val recentlyAdded: List<Wallpaper> = emptyList(),
    val scenery: List<Wallpaper> = emptyList(),
    val dark: List<Wallpaper> = emptyList(),
    val aesthetic: List<Wallpaper> = emptyList(),
    val characters: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
) {
    val isEmpty: Boolean get() = featured.isEmpty() && trending.isEmpty() && scenery.isEmpty()
}

class HomeViewModel : ViewModel() {

    private val repository = ServiceLocator.repository

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Populated by DetailScreen whenever a wallpaper is opened. */
    val recentlyViewed: StateFlow<List<Wallpaper>> = repository.observeRecent(12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavorites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init { refresh() }

    fun refresh() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = runCatching {
                coroutineScope {
                    val daily = async { repository.dailyWallpaper() }
                    val featured = async { repository.loadRail(CategoryCatalog.featuredQueries.take(2), 10) }
                    val trending = async { repository.loadRail(CategoryCatalog.trendingQueries.take(2), 10) }
                    val recent = async { repository.loadRail(CategoryCatalog.recentQueries.take(2), 10) }
                    val scenery = async { repository.loadRail(CategoryCatalog.byId("sc_sakura")?.queries.orEmpty(), 10) }
                    val dark = async { repository.loadRail(CategoryCatalog.byId("col_dark")?.queries.orEmpty(), 10) }
                    val aesthetic = async { repository.loadRail(CategoryCatalog.byId("col_aesthetic")?.queries.orEmpty(), 10) }
                    val characters = async { repository.loadRail(CategoryCatalog.byId("col_girls")?.queries.orEmpty(), 10) }

                    HomeUiState(
                        daily = daily.await(),
                        featured = featured.await(),
                        trending = trending.await(),
                        recentlyAdded = recent.await(),
                        scenery = scenery.await(),
                        dark = dark.await(),
                        aesthetic = aesthetic.await(),
                        characters = characters.await(),
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis(),
                    )
                }
            }
            _state.value = result.getOrElse { error ->
                _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Could not load live wallpapers.",
                )
            }
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }
}
