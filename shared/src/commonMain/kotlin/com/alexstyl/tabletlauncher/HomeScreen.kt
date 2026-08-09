package com.alexstyl.tabletlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Columns2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.ui.components.Text
import com.composables.ui.theme.ComposablesTheme

private val appIconSize = 88.dp
private val appTileHeight = 120.dp
private val gridHorizontalPadding = 56.dp
private val gridHorizontalSpacing = 32.dp
private val gridVerticalSpacing = 28.dp
private val launcherIconColor = Color(0xFF4C5BD5)

@Composable
fun HomeScreen(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
  ComposablesTheme { MaterialTheme { HomeScreenContent(apps, onAppClick, modifier) } }
}

@Composable
private fun HomeScreenContent(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp, Boolean) -> Unit,
    modifier: Modifier,
) {
  var splitScreenMode by rememberSaveable { mutableStateOf(false) }

  Box(modifier.fillMaxSize()) {
    LauncherAppGrid(
        apps = apps,
        onAppClick = { app -> onAppClick(app, splitScreenMode) },
    )
    FloatingActionButton(
        onClick = { splitScreenMode = !splitScreenMode },
        modifier =
            Modifier.align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 32.dp, bottom = 32.dp),
        containerColor =
            if (splitScreenMode) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor =
            if (splitScreenMode) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
    ) {
      Icon(Lucide.Columns2, contentDescription = "Launch apps in split screen")
    }
  }
}

@Composable
fun LauncherAppGrid(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black,
    showWallpaper: Boolean = true,
) {
  val wallpaperProvider = rememberWallpaperProvider()
  val wallpaper = remember(wallpaperProvider) { wallpaperProvider.wallpaper() }

  Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
    if (showWallpaper)
        wallpaper?.let { image ->
          Image(
              bitmap = image,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
          )
        }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val columns =
          ((maxWidth - gridHorizontalPadding * 2 + gridHorizontalSpacing) /
                  (appIconSize + gridHorizontalSpacing))
              .toInt()
              .coerceAtLeast(1)
      val rows =
          ((maxHeight + gridVerticalSpacing) / (appTileHeight + gridVerticalSpacing))
              .toInt()
              .coerceAtLeast(1)
      val pages = apps.chunked(columns * rows).ifEmpty { listOf(emptyList()) }
      val pagerState = rememberPagerState(pageCount = { pages.size })

      HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize(),
      ) { page ->
        LauncherGrid(
            apps = pages[page],
            columns = columns,
            contentColor = contentColor,
            onAppClick = onAppClick,
        )
      }
    }
  }
}

@Composable
private fun LauncherGrid(
    apps: List<LauncherApp>,
    columns: Int,
    contentColor: Color,
    onAppClick: (LauncherApp) -> Unit,
) {
  LazyVerticalGrid(
      columns = GridCells.Fixed(columns),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = gridHorizontalPadding),
      horizontalArrangement = Arrangement.spacedBy(gridHorizontalSpacing),
      verticalArrangement = Arrangement.spacedBy(gridVerticalSpacing, Alignment.CenterVertically),
  ) {
    items(
        items = apps,
        key = { app -> app.packageName to app.activityName },
    ) { app ->
      LauncherAppTile(
          app = app,
          contentColor = contentColor,
          onClick = { onAppClick(app) },
      )
    }
  }
}

@Composable
private fun LauncherAppTile(
    app: LauncherApp,
    contentColor: Color,
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
              Modifier.size(appIconSize)
                  .clip(RoundedCornerShape(20.dp))
                  .background(launcherIconColor),
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
          modifier = Modifier.size(appIconSize).clip(RoundedCornerShape(20.dp)),
      )
    }
    Text(
        text = app.name,
        color = contentColor,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}
