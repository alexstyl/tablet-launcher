package com.alexstyl.tabletlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Columns2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.ui.components.Text

private const val appsPerPage = 24
private val launcherIconColor = Color(0xFF4C5BD5)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
  val installedAppsProvider = rememberInstalledAppsProvider()
  val wallpaperProvider = rememberWallpaperProvider()
  var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
  var wallpaper by remember { mutableStateOf<ImageBitmap?>(null) }
  val pages = apps.chunked(appsPerPage).ifEmpty { listOf(emptyList()) }
  val pagerState = rememberPagerState(pageCount = { pages.size })

  LaunchedEffect(installedAppsProvider) { apps = installedAppsProvider.installedApps() }
  LaunchedEffect(wallpaperProvider) { wallpaper = wallpaperProvider.wallpaper() }

  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
    wallpaper?.let { image ->
      Image(
          bitmap = image,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
      )
    }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
      LauncherGrid(
          apps = pages[page],
          onAppClick = installedAppsProvider::launch,
      )
    }
    LargeFloatingActionButton(
        onClick = {},
        modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
    ) {
      Icon(
          imageVector = Lucide.Columns2,
          contentDescription = "Arrange side by side",
      )
    }
  }
}

@Composable
private fun LauncherGrid(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
) {
  LazyVerticalGrid(
      columns = GridCells.Fixed(6),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 56.dp, vertical = 48.dp),
      horizontalArrangement = Arrangement.spacedBy(32.dp),
      verticalArrangement = Arrangement.spacedBy(28.dp),
  ) {
    items(
        items = apps,
        key = { app -> app.packageName },
    ) { app ->
      LauncherAppTile(
          app = app,
          onClick = { onAppClick(app) },
      )
    }
  }
}

@Composable
private fun LauncherAppTile(
    app: LauncherApp,
    onClick: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (app.icon == null) {
      Box(
          modifier =
              Modifier.size(88.dp).clip(RoundedCornerShape(20.dp)).background(launcherIconColor),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Lucide.Rocket,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = Color.White,
        )
      }
    } else {
      Image(
          bitmap = app.icon,
          contentDescription = null,
          modifier = Modifier.size(88.dp).clip(RoundedCornerShape(20.dp)),
      )
    }
    Text(
        text = app.name,
        color = Color.Black,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}
