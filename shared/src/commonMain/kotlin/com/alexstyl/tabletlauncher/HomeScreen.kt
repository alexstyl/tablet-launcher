package com.alexstyl.tabletlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    folders: List<LauncherFolder>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    onSaveFolder: (folderId: String?, name: String, apps: Set<LauncherApp>) -> Unit,
    onDeleteFolder: (folderId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
  var appAction by remember { mutableStateOf<AppAction?>(null) }
  var showingHiddenApps by remember { mutableStateOf(false) }
  var folderDraft by remember { mutableStateOf<FolderDraft?>(null) }
  var openedFolder by remember { mutableStateOf<LauncherFolder?>(null) }
  val folderAppKeys = folders.flatMapTo(mutableSetOf()) { it.appKeys }
  val topLevelApps = apps.filterNot { it.key in folderAppKeys }
  val folderApps =
      folders.associate { folder -> folder.id to apps.filter { it.key in folder.appKeys } }

  ComposablesTheme {
    MaterialTheme {
      Box(modifier = modifier.fillMaxSize()) {
        LauncherGridPager(
            apps = topLevelApps,
            folders = folders,
            folderApps = folderApps,
            hasHiddenApps = hiddenApps.isNotEmpty(),
            onAppClick = onAppClick,
            onAppLongClick = { app -> appAction = AppAction(app, isHidden = false) },
            onFolderClick = { folder -> openedFolder = folder },
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

        openedFolder?.let { folder ->
          FolderAppsDialog(
              folder = folder,
              apps = apps.filter { it.key in folder.appKeys },
              onDismiss = { openedFolder = null },
              onAppClick = { app ->
                openedFolder = null
                onAppClick(app)
              },
              onAppLongClick = { app -> appAction = AppAction(app, isHidden = false) },
              onEdit = {
                openedFolder = null
                folderDraft =
                    FolderDraft(
                        folderId = folder.id,
                        name = folder.name,
                        selectedApps = apps.filter { it.key in folder.appKeys }.toSet(),
                    )
              },
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
              onCreateFolder = {
                appAction = null
                folderDraft = FolderDraft(selectedApps = setOf(action.app))
              },
          )
        }

        folderDraft?.let { draft ->
          CreateFolderDialog(
              apps = apps,
              draft = draft,
              onDraftChange = { folderDraft = it },
              onDismiss = { folderDraft = null },
              onCreate = { name, selectedApps ->
                folderDraft = null
                onSaveFolder(draft.folderId, name, selectedApps)
              },
              onDelete =
                  draft.folderId?.let { folderId ->
                    {
                      folderDraft = null
                      onDeleteFolder(folderId)
                    }
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
    folders: List<LauncherFolder>,
    folderApps: Map<String, List<LauncherApp>>,
    hasHiddenApps: Boolean,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onFolderClick: (LauncherFolder) -> Unit,
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
          folders.map { folder -> LauncherGridItem.Folder(folder) } +
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
            folderApps = folderApps,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onFolderClick = onFolderClick,
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

private data class FolderDraft(
    val folderId: String? = null,
    val name: String = "New folder",
    val selectedApps: Set<LauncherApp>,
)

private data class AppAction(
    val app: LauncherApp,
    val isHidden: Boolean,
)

private sealed interface LauncherGridItem {
  data class App(val app: LauncherApp) : LauncherGridItem

  data class Folder(val folder: LauncherFolder) : LauncherGridItem

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
            folders = emptyList(),
            folderApps = emptyMap(),
            hasHiddenApps = false,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onFolderClick = {},
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
private fun FolderAppsDialog(
    folder: LauncherFolder,
    apps: List<LauncherApp>,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onEdit: () -> Unit,
) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.16f))
              .combinedClickable(interactionSource = null, indication = null, onClick = onDismiss),
      contentAlignment = Alignment.Center,
  ) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        modifier =
            Modifier.fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .combinedClickable(interactionSource = null, indication = null, onClick = {}),
    ) {
      Box {
        LauncherGridPager(
            apps = apps,
            folders = emptyList(),
            folderApps = emptyMap(),
            hasHiddenApps = false,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onFolderClick = {},
            onHiddenAppsClick = {},
            modifier = Modifier.fillMaxSize().padding(top = 64.dp),
            applySystemBarPadding = false,
        )
        Row(
            modifier =
                Modifier.fillMaxWidth().align(Alignment.TopStart).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = folder.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
          TextButton(onClick = onEdit) { Text(text = "Edit") }
        }
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
    onCreateFolder: () -> Unit,
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
          if (!action.isHidden) {
            TextButton(onClick = onCreateFolder) { Text(text = "Create folder") }
          }
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
    folderApps: Map<String, List<LauncherApp>>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onFolderClick: (LauncherFolder) -> Unit,
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
            is LauncherGridItem.Folder -> item.folder.id
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
                modifier = Modifier,
                onClick = { onAppClick(item.app) },
                onLongClick = { onAppLongClick(item.app) },
            )
        is LauncherGridItem.Folder ->
            FolderTile(
                folder = item.folder,
                apps = folderApps[item.folder.id].orEmpty(),
                appIconSize = appIconSize,
                labelColor = labelColor,
                onClick = { onFolderClick(item.folder) },
            )
        LauncherGridItem.HiddenApps ->
            HiddenAppsTile(
                appIconSize = appIconSize,
                labelColor = labelColor,
                modifier = Modifier,
                onClick = onHiddenAppsClick,
            )
      }
    }
  }
}

@Composable
private fun FolderTile(
    folder: LauncherFolder,
    apps: List<LauncherApp>,
    appIconSize: Dp,
    labelColor: Color,
    onClick: () -> Unit,
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .combinedClickable(interactionSource = null, indication = null, onClick = onClick),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FolderPreview(apps = apps, size = appIconSize)
    Text(
        text = folder.name,
        color = labelColor,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun FolderPreview(
    apps: List<LauncherApp>,
    size: Dp,
) {
  val cellSize = if (size > 64.dp) 30.dp else 22.dp
  val cellSpacing = if (size > 64.dp) 6.dp else 4.dp
  Box(
      modifier =
          Modifier.size(size)
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.9f))
              .border(1.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
      contentAlignment = Alignment.Center,
  ) {
    when (apps.size) {
      0 -> FolderPreviewCell(size = cellSize, app = null)
      1 -> FolderPreviewCell(size = cellSize, app = apps[0])
      2 ->
          Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
            FolderPreviewCell(size = cellSize, app = apps[0])
            FolderPreviewCell(size = cellSize, app = apps[1])
          }
      3 ->
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(cellSpacing),
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
              FolderPreviewCell(size = cellSize, app = apps[0])
              FolderPreviewCell(size = cellSize, app = apps[1])
            }
            FolderPreviewCell(size = cellSize, app = apps[2])
          }
      else ->
          Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
              FolderPreviewCell(size = cellSize, app = apps[0])
              FolderPreviewCell(size = cellSize, app = apps[1])
            }
            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
              FolderPreviewCell(size = cellSize, app = apps[2])
              if (apps.size == 4) {
                FolderPreviewCell(size = cellSize, app = apps[3])
              } else {
                FolderPreviewCount(size = cellSize, remaining = apps.size - 3)
              }
            }
          }
    }
  }
}

@Composable
private fun FolderPreviewCell(
    size: Dp,
    app: LauncherApp?,
) {
  if (app?.icon == null) {
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(7.dp)).background(launcherIconColor),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = Lucide.Rocket,
          contentDescription = null,
          modifier = Modifier.size(size / 2),
          tint = Color.White,
      )
    }
  } else {
    Image(
        bitmap = app.icon,
        contentDescription = null,
        modifier = Modifier.size(size).clip(RoundedCornerShape(7.dp)),
    )
  }
}

@Composable
private fun FolderPreviewCount(
    size: Dp,
    remaining: Int,
) {
  Box(
      modifier = Modifier.size(size).clip(RoundedCornerShape(7.dp)).background(Color.Black),
      contentAlignment = Alignment.Center,
  ) {
    Text(text = "+$remaining", color = Color.White, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun CreateFolderDialog(
    apps: List<LauncherApp>,
    draft: FolderDraft,
    onDraftChange: (FolderDraft) -> Unit,
    onDismiss: () -> Unit,
    onCreate: (String, Set<LauncherApp>) -> Unit,
    onDelete: (() -> Unit)?,
) {
  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp)) {
        Text(text = "Create folder", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = draft.name,
            onValueChange = { onDraftChange(draft.copy(name = it)) },
            label = { Text(text = "Folder name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        Text(
            text = "Choose apps",
            modifier = Modifier.padding(top = 20.dp),
            fontWeight = FontWeight.SemiBold,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 240.dp),
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          items(apps, key = { it.packageName to it.activityName }) { app ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .combinedClickable(
                            interactionSource = null,
                            indication = null,
                            onClick = {
                              val selectedApps =
                                  if (app in draft.selectedApps) draft.selectedApps - app
                                  else draft.selectedApps + app
                              onDraftChange(draft.copy(selectedApps = selectedApps))
                            },
                        )
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Checkbox(checked = app in draft.selectedApps, onCheckedChange = null)
              if (app.icon == null) {
                Box(
                    modifier =
                        Modifier.padding(start = 8.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(launcherIconColor),
                    contentAlignment = Alignment.Center,
                ) {
                  Icon(
                      imageVector = Lucide.Rocket,
                      contentDescription = null,
                      modifier = Modifier.size(20.dp),
                      tint = Color.White,
                  )
                }
              } else {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier =
                        Modifier.padding(start = 8.dp).size(32.dp).clip(RoundedCornerShape(8.dp)),
                )
              }
              Text(text = app.name, modifier = Modifier.padding(start = 8.dp), maxLines = 1)
            }
          }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          onDelete?.let { delete -> TextButton(onClick = delete) { Text(text = "Delete folder") } }
          TextButton(onClick = onDismiss) { Text(text = "Cancel") }
          TextButton(
              enabled = draft.name.isNotBlank() && draft.selectedApps.isNotEmpty(),
              onClick = { onCreate(draft.name.trim(), draft.selectedApps) },
          ) {
            Text(text = if (draft.folderId == null) "Create" else "Save")
          }
        }
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
