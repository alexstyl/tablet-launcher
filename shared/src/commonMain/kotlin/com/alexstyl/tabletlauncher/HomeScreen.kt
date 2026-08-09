package com.alexstyl.tabletlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.ui.components.Text
import com.composables.ui.theme.ComposablesTheme

private val appIconSize = 88.dp
private val appTileHeight = 120.dp
private val pagePadding = 24.dp
private val gridHorizontalPadding = 56.dp
private val gridHorizontalSpacing = 96.dp
private val gridVerticalSpacing = 56.dp
private val launcherIconColor = Color(0xFF4C5BD5)

@Composable
fun HomeScreen(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
) {
  ComposablesTheme {
    MaterialTheme { LauncherGridPager(apps = apps, onAppClick = onAppClick, modifier = modifier) }
  }
}

@Composable
private fun LauncherGridPager(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    modifier: Modifier,
) {
  val wallpaperProvider = rememberWallpaperProvider()
  val wallpaper = remember(wallpaperProvider) { wallpaperProvider.wallpaper() }

  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
    wallpaper?.let { image ->
      Image(
          bitmap = image,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
      )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      val pageWidth = maxWidth - pagePadding * 2
      val pageHeight = maxHeight - pagePadding * 2
      val columns =
          ((pageWidth - gridHorizontalPadding * 2 + gridHorizontalSpacing) /
                  (appIconSize + gridHorizontalSpacing))
              .toInt()
              .coerceIn(1, 6)
      val rows =
          ((pageHeight + gridVerticalSpacing) / (appTileHeight + gridVerticalSpacing))
              .toInt()
              .coerceAtLeast(1)
      val pages = apps.chunked(columns * rows).ifEmpty { listOf(emptyList()) }
      val pagerState = rememberPagerState(pageCount = { pages.size })

      HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(pagePadding),
      ) { page ->
        LauncherGrid(
            apps = pages[page],
            columns = columns,
            onAppClick = onAppClick,
        )
      }

      if (pages.size > 1) {
        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = pagePadding),
        )
      }
    }
  }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(pageCount) { page ->
      Box(
          modifier =
              Modifier.size(if (page == currentPage) 8.dp else 6.dp)
                  .background(
                      color =
                          if (page == currentPage) Color.Black else Color.Black.copy(alpha = 0.35f),
                      shape = CircleShape,
                  ),
      )
    }
  }
}

@Composable
private fun LauncherGrid(
    apps: List<LauncherApp>,
    columns: Int,
    onAppClick: (LauncherApp) -> Unit,
) {
  LazyVerticalGrid(
      columns = GridCells.Fixed(columns),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = gridHorizontalPadding),
      horizontalArrangement = Arrangement.spacedBy(gridHorizontalSpacing),
      verticalArrangement = Arrangement.spacedBy(gridVerticalSpacing),
  ) {
    items(
        items = apps,
        key = { app -> app.packageName to app.activityName },
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
        color = Color.Black,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}
