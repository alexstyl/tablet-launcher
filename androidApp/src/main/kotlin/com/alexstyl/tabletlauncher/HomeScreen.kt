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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.FolderPlus
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Rocket
import com.composables.icons.lucide.Trash2
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
private val launcherIconColor = Color.Black
private val grayscaleColorMatrix = ColorMatrix().apply { setToSaturation(0f) }
private val monochromeColorScheme =
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = Color.Black,
        onSecondary = Color.White,
        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        outline = Color.Black,
    )

private fun Modifier.grayscale(): Modifier = drawWithCache {
  val paint = Paint().apply { colorFilter = ColorFilter.colorMatrix(grayscaleColorMatrix) }
  onDrawWithContent {
    drawContext.canvas.saveLayer(Rect(Offset.Zero, size), paint)
    drawContent()
    drawContext.canvas.restore()
  }
}

@Composable
fun HomeScreen(
    apps: List<LauncherApp>,
    hiddenApps: List<LauncherApp>,
    folders: List<LauncherFolder>,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    onRemoveApp: (LauncherApp) -> Unit,
    onSaveFolder: (folderId: String?, name: String, apps: Set<LauncherApp>) -> Unit,
    onDeleteFolder: (folderId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
  var showingHiddenApps by remember { mutableStateOf(false) }
  var folderDraft by remember { mutableStateOf<FolderDraft?>(null) }
  var openedFolder by remember { mutableStateOf<LauncherFolder?>(null) }
  val folderAppKeys = folders.flatMapTo(mutableSetOf()) { it.appKeys }
  val topLevelApps = apps.filterNot { it.key in folderAppKeys }
  val folderApps =
      folders.associate { folder -> folder.id to apps.filter { it.key in folder.appKeys } }

  ComposablesTheme {
    MaterialTheme(colorScheme = monochromeColorScheme) {
      Box(modifier = modifier.fillMaxSize().grayscale()) {
        LauncherGridPager(
            apps = topLevelApps,
            folders = folders,
            folderApps = folderApps,
            hasHiddenApps = hiddenApps.isNotEmpty(),
            onAppClick = onAppClick,
            onAppInfo = onAppLongClick,
            onHideApp = onHideApp,
            onRestoreApp = onRestoreApp,
            onCreateFolder = { app -> folderDraft = FolderDraft(selectedApps = setOf(app)) },
            onRemoveApp = onRemoveApp,
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
              onAppInfo = onAppLongClick,
              onRestoreApp = onRestoreApp,
              onRemoveApp = onRemoveApp,
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
              onAppInfo = onAppLongClick,
              onHideApp = onHideApp,
              onCreateFolder = { app -> folderDraft = FolderDraft(selectedApps = setOf(app)) },
              onRemoveApp = onRemoveApp,
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
    onAppInfo: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    onCreateFolder: (LauncherApp) -> Unit,
    onRemoveApp: (LauncherApp) -> Unit,
    onFolderClick: (LauncherFolder) -> Unit,
    onHiddenAppsClick: () -> Unit,
    modifier: Modifier,
    applySystemBarPadding: Boolean = true,
    isShowingHiddenApps: Boolean = false,
) {
  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
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
          (folders.map { folder -> LauncherGridItem.Folder(folder) } +
                  apps.map { app -> LauncherGridItem.App(app) } +
                  if (hasHiddenApps) listOf(LauncherGridItem.HiddenApps) else emptyList())
              .sortedBy { it.name.lowercase() }
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
            folderApps = folderApps,
            onAppClick = onAppClick,
            onAppInfo = onAppInfo,
            onHideApp = onHideApp,
            onRestoreApp = onRestoreApp,
            onCreateFolder = onCreateFolder,
            onRemoveApp = onRemoveApp,
            onFolderClick = onFolderClick,
            onHiddenAppsClick = onHiddenAppsClick,
            isShowingHiddenApps = isShowingHiddenApps,
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

private data class FolderDraft(
    val folderId: String? = null,
    val name: String = "New folder",
    val selectedApps: Set<LauncherApp>,
)

private sealed interface LauncherGridItem {
  val name: String

  data class App(val app: LauncherApp) : LauncherGridItem {
    override val name: String = app.name
  }

  data class Folder(val folder: LauncherFolder) : LauncherGridItem {
    override val name: String = folder.name
  }

  data object HiddenApps : LauncherGridItem {
    override val name: String = "Hidden"
  }
}

@Composable
private fun HiddenAppsDialog(
    apps: List<LauncherApp>,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onAppInfo: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    onRemoveApp: (LauncherApp) -> Unit,
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
            onAppInfo = onAppInfo,
            onHideApp = {},
            onRestoreApp = onRestoreApp,
            onCreateFolder = {},
            onRemoveApp = onRemoveApp,
            onFolderClick = {},
            onHiddenAppsClick = {},
            modifier = Modifier.fillMaxSize().padding(top = 64.dp),
            applySystemBarPadding = false,
            isShowingHiddenApps = true,
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
    onAppInfo: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onCreateFolder: (LauncherApp) -> Unit,
    onRemoveApp: (LauncherApp) -> Unit,
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
            onAppInfo = onAppInfo,
            onHideApp = onHideApp,
            onRestoreApp = {},
            onCreateFolder = onCreateFolder,
            onRemoveApp = onRemoveApp,
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
    folderApps: Map<String, List<LauncherApp>>,
    onAppClick: (LauncherApp) -> Unit,
    onAppInfo: (LauncherApp) -> Unit,
    onHideApp: (LauncherApp) -> Unit,
    onRestoreApp: (LauncherApp) -> Unit,
    onCreateFolder: (LauncherApp) -> Unit,
    onRemoveApp: (LauncherApp) -> Unit,
    onFolderClick: (LauncherFolder) -> Unit,
    onHiddenAppsClick: () -> Unit,
    isShowingHiddenApps: Boolean,
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
                modifier = Modifier,
                onClick = { onAppClick(item.app) },
                onInfo = { onAppInfo(item.app) },
                onChangeHiddenState = {
                  if (isShowingHiddenApps) onRestoreApp(item.app) else onHideApp(item.app)
                },
                onCreateFolder =
                    if (isShowingHiddenApps) null
                    else {
                      { onCreateFolder(item.app) }
                    },
                onRemoveApp =
                    if (item.app.canUninstall) {
                      { onRemoveApp(item.app) }
                    } else {
                      null
                    },
                isHidden = isShowingHiddenApps,
            )
        is LauncherGridItem.Folder ->
            FolderTile(
                folder = item.folder,
                apps = folderApps[item.folder.id].orEmpty(),
                appIconSize = appIconSize,
                onClick = { onFolderClick(item.folder) },
            )
        LauncherGridItem.HiddenApps ->
            HiddenAppsTile(
                appIconSize = appIconSize,
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
        color = Color.Black,
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
    Surface(modifier = Modifier.fillMaxSize().grayscale()) {
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
    modifier: Modifier,
    onClick: () -> Unit,
    onInfo: () -> Unit,
    onChangeHiddenState: () -> Unit,
    onCreateFolder: (() -> Unit)?,
    onRemoveApp: (() -> Unit)?,
    isHidden: Boolean,
) {
  var showMenu by remember { mutableStateOf(false) }
  Box(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick,
                    onLongClick = { showMenu = true },
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
          color = Color.Black,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      DropdownMenuItem(
          text = { Text(text = "Info") },
          leadingIcon = {
            Icon(
                imageVector = Lucide.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp))
          },
          onClick = {
            showMenu = false
            onInfo()
          },
      )
      onCreateFolder?.let { createFolder ->
        DropdownMenuItem(
            text = { Text(text = "Create folder") },
            leadingIcon = {
              Icon(
                  imageVector = Lucide.FolderPlus,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
              )
            },
            onClick = {
              showMenu = false
              createFolder()
            },
        )
      }
      DropdownMenuItem(
          text = { Text(text = if (isHidden) "Put back" else "Hide") },
          leadingIcon = {
            Icon(
                imageVector = if (isHidden) Lucide.Eye else Lucide.EyeOff,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
          },
          onClick = {
            showMenu = false
            onChangeHiddenState()
          },
      )
      onRemoveApp?.let { removeApp ->
        DropdownMenuItem(
            text = { Text(text = "Remove app", color = Color.Red) },
            leadingIcon = {
              Icon(
                  imageVector = Lucide.Trash2,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = Color.Red,
              )
            },
            onClick = {
              showMenu = false
              removeApp()
            },
        )
      }
    }
  }
}

@Composable
private fun HiddenAppsTile(
    appIconSize: Dp,
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
        color = Color.Black,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
  }
}
