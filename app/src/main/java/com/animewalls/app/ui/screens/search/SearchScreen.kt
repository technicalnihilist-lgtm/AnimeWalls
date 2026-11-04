package com.animewalls.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animewalls.app.domain.catalog.CategoryCatalog
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.EmptyState
import com.animewalls.app.ui.components.ErrorState
import com.animewalls.app.ui.components.GlassCard
import com.animewalls.app.ui.components.GridPlaceholder
import com.animewalls.app.ui.components.PillChip
import com.animewalls.app.ui.components.SectionHeader
import com.animewalls.app.ui.components.WallpaperGrid
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AccentRed
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceHigh
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary
import com.animewalls.app.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    onOpenWallpaper: (Wallpaper) -> Unit,
    onOpenCategory: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Column(Modifier.fillMaxSize().background(AmoledBlack)) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = CircleShape) {
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = {
                        Text("Anime, character, scenery…", color = TextTertiary, style = MaterialTheme.typography.bodyMedium)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Close, "Clear",
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp).clickable { viewModel.onQueryChange("") },
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus(); viewModel.submit()
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentRed,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
            }
        }

        when {
            state.query.isBlank() -> SearchLanding(onSuggestion = viewModel::useSuggestion, onOpenCategory = onOpenCategory)
            state.isSearching -> GridPlaceholder()
            state.error != null && state.items.isEmpty() ->
                ErrorState(state.error!!, onRetry = viewModel::submit)
            state.items.isEmpty() && state.searched ->
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = "No licensed matches",
                    message = "Nothing on Wikimedia Commons for \"${state.query}\" passed the licence check. Try a broader term like \"Kyoto\", \"sakura\" or \"night\".",
                )
            else -> WallpaperGrid(
                items = state.items,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                hasMore = state.hasMore,
                isLoadingMore = state.isLoadingMore,
                onLoadMore = viewModel::loadMore,
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SearchLanding(onSuggestion: (String) -> Unit, onOpenCategory: (String) -> Unit) {
    val suggestions = remember {
        listOf("cherry blossom", "Mount Fuji", "Kyoto street", "anime girl", "torii gate",
            "Japanese temple", "rain street", "ukiyo-e", "bamboo forest", "Tokyo night",
            "anime boy", "zen garden")
    }
    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        SectionHeader(title = "Try these", subtitle = "Popular live searches")
        Spacer(Modifier.height(10.dp))
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { term ->
                PillChip(text = term, onClick = { onSuggestion(term) })
            }
        }
        Spacer(Modifier.height(26.dp))
        SectionHeader(title = "Browse scenery", subtitle = "Fully licensed Japanese photography")
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(CategoryCatalog.scenery) { category ->
                PillChip(text = category.name, onClick = { onOpenCategory(category.id) })
            }
        }
        Spacer(Modifier.height(26.dp))
        SectionHeader(title = "Collections", subtitle = "Moods and styles")
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(CategoryCatalog.collections) { category ->
                PillChip(text = category.name, onClick = { onOpenCategory(category.id) })
            }
        }
    }
}
