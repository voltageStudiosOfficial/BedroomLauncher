/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package tech.voltagestudios.dream.ui.screens.content.download.assets.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tech.voltagestudios.dream.game.download.assets.platform.Platform
import tech.voltagestudios.dream.game.download.assets.platform.PlatformClasses
import tech.voltagestudios.dream.game.download.assets.platform.curseforge.models.CurseForgeShadersCategory
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthFeatures
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthShadersCategory
import tech.voltagestudios.dream.setting.AllSettings
import tech.voltagestudios.dream.ui.screens.NormalNavKey
import tech.voltagestudios.dream.ui.screens.TitledNavKey

@Composable
fun SearchShadersScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadShadersScreenKey: TitledNavKey,
    downloadShadersScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchShadersPlatform.getValue()
    }
    SearchAssetsScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadShadersScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchShaders,
        currentKey = downloadShadersScreenCurrentKey,
        platformClasses = PlatformClasses.SHADERS,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchShadersPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeShadersCategory.entries
                Platform.MODRINTH -> ModrinthShadersCategory.entries
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthShadersCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeShadersCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload
    )
}