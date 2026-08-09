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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.ui.components.Text
import com.composables.ui.theme.ComposablesTheme

private val compactWidthBreakpoint = 600.dp
private val tabletAppIconSize = 88.dp
private val tabletAppTileHeight = 120.dp
private val tabletPagePadding = 24.dp
private val tabletTopPagePadding = 48.dp
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
    hiddenApps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
) {
  var appAction by remember { mutableStateOf<AppAction?>(null) }
  var showingHiddenApps by remember { mutableStateOf(false) }

  ComposablesTheme {
    MaterialTheme {
      Box(modifier = modifier.fillMaxSize()) {
        LauncherGridPager(
            apps = apps,
            hasHiddenApps = hiddenApps.isNotEmpty(),
            onAppClick = onAppClick,
            onAppLongClick = { app -> appAction = AppAction(app, isHidden = false) },
            onHiddenAppsClick = { showingHiddenApps = true },
            modifier = Modifier.fillMaxSize(),
        )

        if (showingHiddenApps) {
          HiddenAppsDialog(
              apps = hiddenApps,
              onDismiss = { showingHiddenApps = false },
              onAppClick = { app ->
                showingHiddenApps = false
                onAppClick(app)
              },
              onAppLongClick = { app -> appAction = AppAction(app, isHidden = true) },
          )
        }

        appAction?.let { action ->
          AppActionsDialog(
              action = action,
              onDismiss = { appAction = null },
              onInfo = {
                appAction = null
                onAppLongClick(action.app)
              },
              onChangeHiddenState = {
                appAction = null
                if (action.isHidden) onRestoreApp(action.app) else onHideApp(action.app)
              },
          )
        }
      }
    }
  }
}

@Composable
private fun LauncherGridPager(
    apps: List<LauncherApp>,
    hasHiddenApps: Boolean,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onHiddenAppsClick: () -> Unit,
    modifier: Modifier,
    applySystemBarPadding: Boolean = true,
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
    val gridModifier =
        if (applySystemBarPadding) Modifier.fillMaxSize().systemBarsPadding()
        else Modifier.fillMaxSize()
    BoxWithConstraints(modifier = gridModifier) {
      val isCompact = maxWidth < compactWidthBreakpoint
      val appIconSize = if (isCompact) phoneAppIconSize else tabletAppIconSize
      val appTileHeight = if (isCompact) phoneAppTileHeight else tabletAppTileHeight
      val pagePadding = if (isCompact) phonePagePadding else tabletPagePadding
      val topPagePadding = if (isCompact) phonePagePadding else tabletTopPagePadding
      val gridHorizontalPadding =
          if (isCompact) phoneGridHorizontalPadding else tabletGridHorizontalPadding
      val gridHorizontalSpacing =
          if (isCompact) phoneGridHorizontalSpacing else tabletGridHorizontalSpacing
      val gridVerticalSpacing =
          if (isCompact) phoneGridVerticalSpacing else tabletGridVerticalSpacing
      val pageWidth = maxWidth - pagePadding * 2
      val pageHeight = maxHeight - topPagePadding - pagePadding
      val columns =
          ((pageWidth - gridHorizontalPadding * 2 + gridHorizontalSpacing) /
                  (appIconSize + gridHorizontalSpacing))
              .toInt()
              .coerceIn(1, if (isCompact) 4 else 6)
      val rows =
          ((pageHeight + gridVerticalSpacing) / (appTileHeight + gridVerticalSpacing))
              .toInt()
              .coerceAtLeast(1)
      val gridItems =
          apps.map { app -> LauncherGridItem.App(app) } +
              if (hasHiddenApps) listOf(LauncherGridItem.HiddenApps) else emptyList()
      val pages = gridItems.chunked(columns * rows).ifEmpty { listOf(emptyList()) }
      val pagerState = rememberPagerState(pageCount = { pages.size })

      HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize(),
          contentPadding =
              PaddingValues(
                  start = pagePadding,
                  top = topPagePadding,
                  end = pagePadding,
                  bottom = pagePadding,
              ),
      ) { page ->
        LauncherGrid(
            items = pages[page],
            columns = columns,
            appIconSize = appIconSize,
            gridHorizontalPadding = gridHorizontalPadding,
            gridHorizontalSpacing = gridHorizontalSpacing,
            gridVerticalSpacing = gridVerticalSpacing,
            labelColor = labelColor,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onHiddenAppsClick = onHiddenAppsClick,
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

private data class AppAction(
    val app: LauncherApp,
    val isHidden: Boolean,
)

private sealed interface LauncherGridItem {
  data class App(val app: LauncherApp) : LauncherGridItem

  data object HiddenApps : LauncherGridItem
}

@Composable
private fun HiddenAppsDialog(
    apps: List<LauncherApp>,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.16f))
              .combinedClickable(
                  interactionSource = null,
                  indication = null,
                  onClick = onDismiss,
              ),
      contentAlignment = Alignment.Center,
  ) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        modifier =
            Modifier.fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = {},
                ),
    ) {
      Box {
        LauncherGridPager(
            apps = apps,
            hasHiddenApps = false,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onHiddenAppsClick = {},
            modifier = Modifier.fillMaxSize().padding(top = 64.dp),
            applySystemBarPadding = false,
        )
        Text(
            text = "Hidden Apps",
            modifier = Modifier.align(Alignment.TopStart).padding(24.dp),
            fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun AppActionsDialog(
    action: AppAction,
    onDismiss: () -> Unit,
    onInfo: () -> Unit,
    onChangeHiddenState: () -> Unit,
) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.16f))
              .combinedClickable(
                  interactionSource = null,
                  indication = null,
                  onClick = onDismiss,
              ),
      contentAlignment = Alignment.Center,
  ) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        modifier =
            Modifier.fillMaxWidth(0.8f)
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = {},
                ),
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = action.app.name,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          TextButton(onClick = onInfo) { Text(text = "Info") }
          TextButton(onClick = onChangeHiddenState) {
            Text(text = if (action.isHidden) "Put back" else "Hide")
          }
        }
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
    items: List<LauncherGridItem>,
    columns: Int,
    appIconSize: Dp,
    gridHorizontalPadding: Dp,
    gridHorizontalSpacing: Dp,
    gridVerticalSpacing: Dp,
    labelColor: Color,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onHiddenAppsClick: () -> Unit,
) {
  LazyVerticalGrid(
      columns = GridCells.Fixed(columns),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = gridHorizontalPadding),
      horizontalArrangement = Arrangement.spacedBy(gridHorizontalSpacing),
      verticalArrangement = Arrangement.spacedBy(gridVerticalSpacing),
  ) {
    items(
        items = items,
        key = { item ->
          when (item) {
            is LauncherGridItem.App -> item.app.packageName to item.app.activityName
            LauncherGridItem.HiddenApps -> "hidden_apps"
          }
        },
    ) { item ->
      when (item) {
        is LauncherGridItem.App ->
            LauncherAppTile(
                app = item.app,
                appIconSize = appIconSize,
                labelColor = labelColor,
                modifier = Modifier.animateItem(),
                onClick = { onAppClick(item.app) },
                onLongClick = { onAppLongClick(item.app) },
            )
        LauncherGridItem.HiddenApps ->
            HiddenAppsTile(
                appIconSize = appIconSize,
                labelColor = labelColor,
                modifier = Modifier.animateItem(),
                onClick = onHiddenAppsClick,
            )
      }
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

@Composable
private fun HiddenAppsTile(
    appIconSize: Dp,
    labelColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .combinedClickable(
                  interactionSource = null,
                  indication = null,
                  onClick = onClick,
              ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
        modifier =
            Modifier.size(appIconSize)
                .clip(RoundedCornerShape(20.dp))
                .background(launcherIconColor),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = Lucide.EyeOff,
          contentDescription = null,
          modifier = Modifier.size(44.dp),
          tint = Color.White,
      )
    }
    Text(
        text = "Hidden",
        color = labelColor,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
  }
}
