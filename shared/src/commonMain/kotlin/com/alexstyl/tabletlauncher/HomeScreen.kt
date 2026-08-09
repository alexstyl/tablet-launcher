package com.alexstyl.tabletlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.ui.components.Text
import com.composables.ui.theme.ComposablesTheme

private val compactWidthBreakpoint = 600.dp
private val tabletAppIconSize = 88.dp
private val tabletAppTileHeight = 120.dp
private val tabletPagePadding = 24.dp
private val tabletGridHorizontalPadding = 56.dp
private val tabletGridHorizontalSpacing = 96.dp
private val tabletGridVerticalSpacing = 56.dp
private val phoneAppIconSize = 64.dp
private val phoneAppTileHeight = 100.dp
private val phonePagePadding = 16.dp
private val phoneGridHorizontalPadding = 16.dp
private val phoneGridHorizontalSpacing = 16.dp
private val phoneGridVerticalSpacing = 32.dp
private val launcherIconColor = Color(0xFF4C5BD5)

@Composable
fun HomeScreen(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
) {
  ComposablesTheme {
    MaterialTheme {
      LauncherGridPager(
          apps = apps,
          onAppClick = onAppClick,
          onAppLongClick = onAppLongClick,
          modifier = modifier,
      )
    }
  }
}

@Composable
private fun LauncherGridPager(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    modifier: Modifier,
) {
  val wallpaperProvider = rememberWallpaperProvider()
  val wallpaper = remember(wallpaperProvider) { wallpaperProvider.wallpaper() }
  val labelColor = remember(wallpaper) { wallpaper.labelColor() }

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
      val isCompact = maxWidth < compactWidthBreakpoint
      val appIconSize = if (isCompact) phoneAppIconSize else tabletAppIconSize
      val appTileHeight = if (isCompact) phoneAppTileHeight else tabletAppTileHeight
      val pagePadding = if (isCompact) phonePagePadding else tabletPagePadding
      val gridHorizontalPadding =
          if (isCompact) phoneGridHorizontalPadding else tabletGridHorizontalPadding
      val gridHorizontalSpacing =
          if (isCompact) phoneGridHorizontalSpacing else tabletGridHorizontalSpacing
      val gridVerticalSpacing =
          if (isCompact) phoneGridVerticalSpacing else tabletGridVerticalSpacing
      val pageWidth = maxWidth - pagePadding * 2
      val pageHeight = maxHeight - pagePadding * 2
      val columns =
          ((pageWidth - gridHorizontalPadding * 2 + gridHorizontalSpacing) /
                  (appIconSize + gridHorizontalSpacing))
              .toInt()
              .coerceIn(1, if (isCompact) 4 else 6)
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
            appIconSize = appIconSize,
            gridHorizontalPadding = gridHorizontalPadding,
            gridHorizontalSpacing = gridHorizontalSpacing,
            gridVerticalSpacing = gridVerticalSpacing,
            labelColor = labelColor,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
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

private fun ImageBitmap?.labelColor(): Color {
  if (this == null) {
    return Color.Black
  }

  val samplesPerAxis = 5
  val averageLuminance =
      (1..samplesPerAxis).sumOf { horizontalSample ->
        (1..samplesPerAxis).sumOf { verticalSample ->
          toPixelMap(
                  startX = width * horizontalSample / (samplesPerAxis + 1),
                  startY = height * verticalSample / (samplesPerAxis + 1),
                  width = 1,
                  height = 1,
              )[0, 0]
              .luminance()
              .toDouble()
        }
      } / (samplesPerAxis * samplesPerAxis)

  return if (averageLuminance > 0.55) Color.Black else Color.White
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
    appIconSize: Dp,
    gridHorizontalPadding: Dp,
    gridHorizontalSpacing: Dp,
    gridVerticalSpacing: Dp,
    labelColor: Color,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
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
          appIconSize = appIconSize,
          labelColor = labelColor,
          modifier = Modifier.animateItem(),
          onClick = { onAppClick(app) },
          onLongClick = { onAppLongClick(app) },
      )
    }
  }
}

@Composable
private fun LauncherAppTile(
    app: LauncherApp,
    appIconSize: Dp,
    labelColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .combinedClickable(
                  interactionSource = null,
                  indication = null,
                  onClick = onClick,
                  onLongClick = onLongClick,
              ),
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
        color = labelColor,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}
