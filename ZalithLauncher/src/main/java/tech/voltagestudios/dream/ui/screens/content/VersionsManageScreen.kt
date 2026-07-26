/*
 * Bedroom Launcher (BL)
 * Copyright (C) 2025 Voltage Studios Official <voltageStudiosOfficial@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package tech.voltagestudios.dream.ui.screens.content

import android.os.Environment
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.scrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.voltagestudios.dream.R
import tech.voltagestudios.dream.game.path.GamePathManager
import tech.voltagestudios.dream.game.version.installed.Version
import tech.voltagestudios.dream.game.version.installed.VersionComparator
import tech.voltagestudios.dream.game.version.installed.VersionType
import tech.voltagestudios.dream.game.version.installed.VersionsManager
import tech.voltagestudios.dream.game.version.installed.cleanup.GameAssetCleaner
import tech.voltagestudios.dream.ui.activities.MainActivity
import tech.voltagestudios.dream.ui.base.BaseScreen
import tech.voltagestudios.dream.ui.components.BackgroundCard
import tech.voltagestudios.dream.ui.components.CardTitleLayout
import tech.voltagestudios.dream.ui.components.EdgeDirection
import tech.voltagestudios.dream.ui.components.IconTextButton
import tech.voltagestudios.dream.ui.components.MarqueeText
import tech.voltagestudios.dream.ui.components.ScalingActionButton
import tech.voltagestudios.dream.ui.components.ScalingLabel
import tech.voltagestudios.dream.ui.components.fadeEdge
import tech.voltagestudios.dream.ui.screens.NormalNavKey
import tech.voltagestudios.dream.ui.screens.content.elements.CleanupOperation
import tech.voltagestudios.dream.ui.screens.content.elements.GamePathItemLayout
import tech.voltagestudios.dream.ui.screens.content.elements.GamePathOperation
import tech.voltagestudios.dream.ui.screens.content.elements.VersionCategory
import tech.voltagestudios.dream.ui.screens.content.elements.VersionCategoryItem
import tech.voltagestudios.dream.ui.screens.content.elements.VersionItemLayout
import tech.voltagestudios.dream.ui.screens.content.elements.VersionsOperation
import tech.voltagestudios.dream.utils.animation.swapAnimateDpAsState
import tech.voltagestudios.dream.utils.canHandlePermission
import tech.voltagestudios.dream.utils.checkStoragePermissions
import tech.voltagestudios.dream.viewmodel.ErrorViewModel
import tech.voltagestudios.dream.viewmodel.EventViewModel
import tech.voltagestudios.dream.viewmodel.ScreenBackStackViewModel
import tech.voltagestudios.dream.viewmodel.sendKeepScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private class VersionsScreenViewModel : ViewModel() {
    /** 版本类别分类 */
    var versionCategory by mutableStateOf(VersionCategory.ALL)
        private set
    /** 重排序刷新key */
    var resortKey by mutableIntStateOf(0)
        private set

    /** 游戏路径相关操作 */
    var gamePathOperation by mutableStateOf<GamePathOperation>(GamePathOperation.None)

    /** 全部版本的数量 */
    var allVersionsCount by mutableIntStateOf(0)
    /** 原版版本数量 */
    var vanillaVersionsCount by mutableIntStateOf(0)
    /** 模组加载器版本数量 */
    var modloaderVersionsCount by mutableIntStateOf(0)

    fun startRefreshVersions() {
        if (!VersionsManager.isRefreshing.value) {
            VersionsManager.refresh("VersionsScreenViewModel.startRefreshVersions")
        }
    }

    private var currentJob: Job? = null
    private var mutex: Mutex = Mutex()

    /**
     * 变更当前版本列表的过滤类型
     */
    fun changeCategory(category: VersionCategory) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            mutex.withLock {
                this@VersionsScreenViewModel.versionCategory = category
            }
        }
    }

    /**
     * 重新排序当前版本列表
     */
    fun resortVersions() {
        resortKey++
    }

    /** 清理游戏文件操作 */
    var cleanupOperation by mutableStateOf<CleanupOperation>(CleanupOperation.None)

    /** 游戏无用资源清理者 */
    var cleaner by mutableStateOf<GameAssetCleaner?>(null)

    fun cleanUnusedFiles(
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        cleaner = GameAssetCleaner(
            scope = viewModelScope
        ).also {
            cleanupOperation = CleanupOperation.Clean
            it.start(
                onEnd = { count, size ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Success(count, size)
                    onStop()
                },
                onThrowable = { th ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Error(th)
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancelCleaner() {
        cleaner?.cancel()
        cleaner = null
        cleanupOperation = CleanupOperation.None
    }

    override fun onCleared() {
        cancelCleaner()
        currentJob?.cancel()
    }
}

@Composable
private fun rememberVersionViewModel() : VersionsScreenViewModel {
    return viewModel(
        key = NormalNavKey.VersionsManager.toString()
    ) {
        VersionsScreenViewModel()
    }
}

@Composable
private fun rememberVersions(
    versions: StateFlow<List<Version>>,
    viewModel: VersionsScreenViewModel,
): State<List<Version>> {
    val vers by versions.collectAsStateWithLifecycle()
    val category = viewModel.versionCategory
    val resortKey = viewModel.resortKey

    return remember(vers, category, resortKey) {
        derivedStateOf {
            viewModel.allVersionsCount = vers.size

            val vanillaVersions = vers
                .filter { ver -> ver.versionType == VersionType.VANILLA }
                .also { viewModel.vanillaVersionsCount = it.size }
            val modloaderVersions = vers
                .filter { ver -> ver.versionType == VersionType.MODLOADERS }
                .also { viewModel.modloaderVersionsCount = it.size }

            when (category) {
                VersionCategory.ALL -> vers
                VersionCategory.VANILLA -> vanillaVersions
                VersionCategory.MODLOADER -> modloaderVersions
            }.sortedWith(VersionComparator)
        }
    }
}

@Composable
fun VersionsManageScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    navigateToExport: (Version) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val viewModel = rememberVersionViewModel()

    val versions by rememberVersions(VersionsManager.versions, viewModel)
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    GamePathOperation(
        gamePathOperation = viewModel.gamePathOperation,
        changeState = { viewModel.gamePathOperation = it },
        submitError = submitError
    )

    BaseScreen(
        screenKey = NormalNavKey.VersionsManager,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row {
            LeftMenu(
                isVisible = isVisible,
                isRefreshing = isRefreshing,
                swapToFileSelector = { path ->
                    backScreenViewModel.mainScreen.backStack.navigateToFileSelector(
                        startPath = path,
                        selectFile = false,
                        saveKey = NormalNavKey.VersionsManager
                    ) { path ->
                        viewModel.gamePathOperation = GamePathOperation.AddNewPath(path)
                    }
                },
                onCleanupGameFiles = {
                    if (viewModel.cleanupOperation == CleanupOperation.None) {
                        viewModel.cleanupOperation = CleanupOperation.Tip
                    }
                },
                changePathOperation = {
                    viewModel.gamePathOperation = it
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
            )

            VersionsLayout(
                isVisible = isVisible,
                isRefreshing = isRefreshing,
                versions = versions,
                currentVersion = currentVersion,
                versionCategory = viewModel.versionCategory,
                onCategoryChange = { viewModel.changeCategory(it) },
                allVersionsCount = viewModel.allVersionsCount,
                vanillaVersionsCount = viewModel.vanillaVersionsCount,
                modloaderVersionsCount = viewModel.modloaderVersionsCount,
                navigateToVersions = navigateToVersions,
                navigateToExport = navigateToExport,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7.5f)
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp),
                submitError = submitError,
                onRefresh = {
                    viewModel.startRefreshVersions()
                },
                onVersionPinned = {
                    viewModel.resortVersions()
                },
                onInstall = {
                    backScreenViewModel.navigateToDownload()
                }
            )

            CleanupOperation(
                operation = viewModel.cleanupOperation,
                changeOperation = { viewModel.cleanupOperation = it },
                cleaner = viewModel.cleaner,
                onClean = {
                    viewModel.cleanUnusedFiles(
                        onStart = {
                            eventViewModel.sendKeepScreen(true)
                        },
                        onStop = {
                            eventViewModel.sendKeepScreen(false)
                        }
                    )
                },
                onCancel = {
                    viewModel.cancelCleaner()
                    eventViewModel.sendKeepScreen(false)
                },
                submitError = submitError
            )
        }
    }
}

@Composable
private fun LeftMenu(
    isVisible: Boolean,
    isRefreshing: Boolean,
    swapToFileSelector: (path: String) -> Unit,
    onCleanupGameFiles: () -> Unit,
    changePathOperation: (GamePathOperation) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    Column(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
    ) {
        val gamePaths by GamePathManager.gamePathData.collectAsStateWithLifecycle()
        val currentPath by GamePathManager.currentPath.collectAsStateWithLifecycle()
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(gamePaths, key = { it.id }) { pathItem ->
                GamePathItemLayout(
                    item = pathItem,
                    selected = currentPath == pathItem.path,
                    enabled = canHandlePermission,
                    onClick = {
                        if (!isRefreshing) { //避免频繁刷新，防止currentGameInfo意外重置
                            if (pathItem.id == GamePathManager.DEFAULT_ID) {
                                GamePathManager.saveDefaultPath()
                            } else {
                                (context as? MainActivity)?.let { activity ->
                                    checkStoragePermissions(
                                        activity = activity,
                                        message = activity.getString(R.string.versions_manage_game_storage_permissions),
                                        messageSdk30 = activity.getString(R.string.versions_manage_game_storage_permissions_sdk30),
                                        hasPermission = {
                                            GamePathManager.saveCurrentPath(pathItem.id)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    onDelete = {
                        changePathOperation(GamePathOperation.DeletePath(pathItem))
                    },
                    onRename = {
                        changePathOperation(GamePathOperation.RenamePath(pathItem))
                    }
                )
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .fillMaxWidth(),
            onClick = {
                (context as? MainActivity)?.let { activity ->
                    checkStoragePermissions(
                        activity = activity,
                        message = activity.getString(R.string.versions_manage_game_path_storage_permissions),
                        messageSdk30 = activity.getString(R.string.versions_manage_game_path_storage_permissions_sdk30),
                        hasPermission = {
                            swapToFileSelector(Environment.getExternalStorageDirectory().absolutePath)
                        }
                    )
                }
            },
            enabled = canHandlePermission
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_game_path_add_new))
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = onCleanupGameFiles
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_cleanup))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VersionsLayout(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isRefreshing: Boolean,
    versions: List<Version>,
    currentVersion: Version?,
    versionCategory: VersionCategory,
    onCategoryChange: (VersionCategory) -> Unit,
    allVersionsCount: Int,
    vanillaVersionsCount: Int,
    modloaderVersionsCount: Int,
    navigateToVersions: (Version) -> Unit,
    navigateToExport: (Version) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onRefresh: () -> Unit,
    onVersionPinned: () -> Unit,
    onInstall: () -> Unit,
) {
    val surfaceYOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = surfaceYOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isRefreshing) { //版本正在刷新中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
            VersionsOperation(
                versionsOperation = versionsOperation,
                updateVersionsOperation = { versionsOperation = it },
                submitError = submitError
            )

            Column(modifier = Modifier.fillMaxSize()) {
                CardTitleLayout {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fadeEdge(
                                state = scrollState,
                                length = 32.dp,
                                direction = EdgeDirection.Horizontal
                            )
                            .fillMaxWidth()
                            .horizontalScroll(state = scrollState)
                            .padding(all = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTextButton(
                            onClick = onRefresh,
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh),
                            text = stringResource(R.string.generic_refresh)
                        )
                        IconTextButton(
                            onClick = onInstall,
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = stringResource(R.string.versions_manage_install_new),
                            text = stringResource(R.string.versions_manage_install_new),
                        )
                        //版本分类
                        VersionCategoryItem(
                            value = VersionCategory.ALL,
                            versionsCount = allVersionsCount,
                            selected = versionCategory == VersionCategory.ALL,
                            onClick = { onCategoryChange(VersionCategory.ALL) }
                        )
                        VersionCategoryItem(
                            value = VersionCategory.VANILLA,
                            versionsCount = vanillaVersionsCount,
                            selected = versionCategory == VersionCategory.VANILLA,
                            onClick = { onCategoryChange(VersionCategory.VANILLA) }
                        )
                        VersionCategoryItem(
                            value = VersionCategory.MODLOADER,
                            versionsCount = modloaderVersionsCount,
                            selected = versionCategory == VersionCategory.MODLOADER,
                            onClick = { onCategoryChange(VersionCategory.MODLOADER) }
                        )
                    }
                }

                if (versions.isNotEmpty()) {
                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .scrollbar(
                                state = scrollState.scrollIndicatorState,
                                orientation = Orientation.Vertical,
                            )
                            .clipToBounds(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        state = scrollState,
                    ) {
                        items(versions, key = { it.toString() }) { version ->
                            VersionItemLayout(
                                version = version,
                                selected = version == currentVersion,
                                submitError = submitError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .animateItem(),
                                onSelected = {
                                    if (version == currentVersion) return@VersionItemLayout
                                    if (!VersionsManager.saveVersion(version)) {
                                        //不允许选择无效版本
                                        versionsOperation = VersionsOperation.InvalidDelete(version)
                                    }
                                },
                                onSettingsClick = {
                                    navigateToVersions(version)
                                },
                                onRenameClick = { versionsOperation = VersionsOperation.Rename(version) },
                                onCopyClick = { versionsOperation = VersionsOperation.Copy(version) },
                                onExportClick = { navigateToExport(version) },
                                onDeleteClick = { versionsOperation = VersionsOperation.Delete(version) },
                                onPinned = onVersionPinned
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.versions_manage_no_versions)
                        )
                    }
                }
            }
        }
    }
}