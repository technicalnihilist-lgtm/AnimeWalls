package com.animewalls.app.ui.navigation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.animewalls.app.ui.screens.category.CategoryScreen
import com.animewalls.app.ui.screens.credits.CreditsScreen
import com.animewalls.app.ui.screens.detail.DetailScreen
import com.animewalls.app.ui.screens.home.HomeScreen
import com.animewalls.app.ui.screens.library.DownloadsScreen
import com.animewalls.app.ui.screens.library.FavoritesScreen
import com.animewalls.app.ui.screens.search.SearchScreen
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AccentRed
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceGlass
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextTertiary

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val DOWNLOADS = "downloads"
    const val CREDITS = "credits"
    const val CATEGORY = "category/{categoryId}"
    const val DETAIL = "detail/{wallpaperId}"
    fun category(id: String) = "category/$id"
    fun detail(id: String) = "detail/$id"
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Filled.Home),
    TabItem(Routes.SEARCH, "Search", Icons.Filled.Search),
    TabItem(Routes.FAVORITES, "Saved", Icons.Filled.FavoriteBorder),
    TabItem(Routes.DOWNLOADS, "Offline", Icons.Filled.KeyboardArrowDown),
)

@Composable
fun AnimeWallsNav(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(Modifier.fillMaxSize().background(AmoledBlack)) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onOpenCategory = { navController.navigate(Routes.category(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenCredits = { navController.navigate(Routes.CREDITS) },
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onOpenCategory = { navController.navigate(Routes.category(it)) },
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onBrowse = { navController.navigate(Routes.HOME) },
                )
            }
            composable(Routes.DOWNLOADS) {
                DownloadsScreen(
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onBrowse = { navController.navigate(Routes.HOME) },
                )
            }
            composable(Routes.CREDITS) {
                CreditsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.CATEGORY,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            ) {
                CategoryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType }),
            ) {
                DetailScreen(onBack = { navController.popBackStack() })
            }
        }

        if (currentRoute in tabs.map { it.route }) {
            BottomBar(
                currentRoute = currentRoute,
                onSelect = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceGlass)
                .border(1.dp, Hairline, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab.route) }
                        .padding(vertical = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (selected) AccentRed.copy(alpha = 0.16f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) AccentPink else TextTertiary,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) AccentPink else TextTertiary,
                    )
                }
            }
        }
    }
}
