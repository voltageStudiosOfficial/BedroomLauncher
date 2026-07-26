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

package tech.voltagestudios.dream.ui.screens.content.download.assets.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tech.voltagestudios.dream.game.download.assets.platform.Platform
import tech.voltagestudios.dream.game.download.assets.platform.PlatformClasses
import tech.voltagestudios.dream.game.download.assets.platform.curseforge.models.CurseForgeModCategory
import tech.voltagestudios.dream.game.download.assets.platform.curseforge.models.curseForgeModLoaderFilters
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthFeatures
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthModCategory
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.modrinthModLoaderFilters
import tech.voltagestudios.dream.setting.AllSettings
import tech.voltagestudios.dream.ui.screens.NormalNavKey
import tech.voltagestudios.dream.ui.screens.TitledNavKey

@Composable
fun SearchModScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadModScreenKey: TitledNavKey,
    downloadModScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchModPlatform.getValue()
    }
    SearchAssetsScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadModScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchMod,
        currentKey = downloadModScreenCurrentKey,
        platformClasses = PlatformClasses.MOD,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchModPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeModCategory.entries
                Platform.MODRINTH -> ModrinthModCategory.entries
            }
        },
        enableModLoader = true,
        getModloaders = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> curseForgeModLoaderFilters
                Platform.MODRINTH -> modrinthModLoaderFilters
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthModCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeModCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload
    )
}