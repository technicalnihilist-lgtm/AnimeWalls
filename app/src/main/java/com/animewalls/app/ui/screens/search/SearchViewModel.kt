package com.animewalls.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animewalls.app.di.ServiceLocator
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val items: List<Wallpaper> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val searched: Boolean = false,
)

class SearchViewModel : ViewModel() {

    private val repository = ServiceLocator.repository

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavorites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var searchJob: Job? = null
    private var offset = 0
    private val seen = LinkedHashSet<String>()

    val suggestions = listOf(
        "cherry blossom", "Mount Fuji", "Kyoto street", "anime girl",
        "torii gate", "Japanese temple", "rain street", "ukiyo-e",
        "bamboo forest", "Tokyo night", "anime boy", "zen garden",
    )

    fun onQueryChange(value: String) {
        _state.value = _state.value.copy(query = value)
        searchJob?.cancel()
        if (value.isBlank()) {
            _state.value = SearchUiState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce so we don't hit the API on every keystroke
            runSearch(value)
        }
    }

    fun submit() {
        val query = _state.value.query
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(query) }
    }

    fun useSuggestion(value: String) {
        _state.value = _state.value.copy(query = value)
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(value) }
    }

    private suspend fun runSearch(query: String) {
        offset = 0
        seen.clear()
        _state.value = _state.value.copy(
            isSearching = true, error = null, items = emptyList(), searched = false, hasMore = false,
        )
        val page = repository.search(query, 0, PAGE_SIZE)
        val fresh = page.items.filter { seen.add(it.id) }
        offset = page.nextOffset
        _state.value = _state.value.copy(
            items = fresh,
            isSearching = false,
            hasMore = page.hasMore,
            error = page.error,
            searched = true,
        )
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore || current.isSearching) return
        viewModelScope.launch {
            _state.value = current.copy(isLoadingMore = true)
            val page = repository.search(current.query, offset, PAGE_SIZE)
            val fresh = page.items.filter { seen.add(it.id) }
            offset = page.nextOffset
            _state.value = _state.value.copy(
                items = _state.value.items + fresh,
                isLoadingMore = false,
                hasMore = page.hasMore && fresh.isNotEmpty(),
            )
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.toggleFavorite(wallpaper) }
    }

    companion object { private const val PAGE_SIZE = 24 }
}
