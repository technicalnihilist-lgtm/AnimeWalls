package com.animewalls.app.ui.screens.credits

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animewalls.app.di.ServiceLocator
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.GlassCard
import com.animewalls.app.ui.components.MetaRow
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary
import com.animewalls.app.ui.theme.TextTertiary
import kotlinx.coroutines.launch

class CreditsViewModel : ViewModel() {
    var items by mutableStateOf<List<Wallpaper>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            items = runCatching { ServiceLocator.repository.creditSources() }.getOrDefault(emptyList())
            loading = false
        }
    }
}

@Composable
fun CreditsScreen(
    onBack: () -> Unit,
    viewModel: CreditsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().background(AmoledBlack)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceMid)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Credits & licences", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Text(
                    "Every image, attributed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                )
            }
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "ABOUT THIS FEED",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentPink,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "AnimeWalls displays artwork served live by the Wikimedia Commons API. " +
                                "Nothing is bundled with the app. Every image shown is filtered to a licence " +
                                "allow-list — CC0, public domain, CC BY and CC BY-SA — and images depicting " +
                                "copyrighted characters, scans, merchandise and adult material are excluded. " +
                                "Images are displayed unmodified; thumbnails are generated by Wikimedia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "AnimeWalls is not affiliated with Wikimedia, any anime studio or publisher.",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                    }
                }
            }

            if (viewModel.items.isEmpty() && !viewModel.loading) {
                item {
                    Text(
                        "Images you view, favourite or download appear here with their full attribution.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                    )
                }
            }

            items(viewModel.items, key = { it.id }) { wallpaper ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            wallpaper.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(8.dp))
                        MetaRow("Artist", wallpaper.artist)
                        MetaRow("Licence", wallpaper.licenseName.ifBlank { wallpaper.licenseShortName })
                        MetaRow("Resolution", wallpaper.resolution)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CreditLink("Source page") {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(wallpaper.sourceUrl))
                                    )
                                }
                            }
                            if (wallpaper.licenseUrl.isNotBlank()) {
                                CreditLink("Licence") {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(wallpaper.licenseUrl))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditLink(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceMid)
            .border(1.dp, Hairline, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AccentPink)
    }
}
