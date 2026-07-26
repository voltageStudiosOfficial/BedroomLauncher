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

package tech.voltagestudios.dream.game.download.assets.platform.modrinth

import tech.voltagestudios.dream.game.download.assets.platform.PlatformClasses
import tech.voltagestudios.dream.game.download.assets.platform.PlatformSearchFilter
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthFacet
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import tech.voltagestudios.dream.game.download.assets.platform.modrinth.models.VersionFacet
import tech.voltagestudios.dream.utils.string.isNotEmptyOrBlank

/**
 * Modrinth 平台的 API 链接
 * [Modrinth Docs](https://docs.modrinth.com/api/operations/searchprojects)
 */
const val MODRINTH_API = "https://api.modrinth.com/v2"

/**
 * MCIM 镜像：Modrinth 平台的 API 链接
 * [MCIM Modrinth API](https://github.com/mcmod-info-mirror/mcim-rust-api?tab=readme-ov-file#modrinth)
 */
const val MCIM_MODRINTH_API = "https://mod.mcimirror.top/modrinth/v2"

fun PlatformSearchFilter.toModrinthRequest(
    query: String,
    platformClasses: PlatformClasses
): ModrinthSearchRequest {
    val modrinthVersion = gameVersion.takeIf { it.isNotEmptyOrBlank() }?.let { version ->
        VersionFacet(version.trim())
    }
    val modrinthCategories = categories.map { category ->
        category as? ModrinthFacet
    }.toTypedArray()
    val modrinthModLoader = modloader?.let { modloader ->
        modloader as? ModrinthModLoaderCategory
    }

    return ModrinthSearchRequest(
        query = query,
        facets = listOfNotNull(
            platformClasses.modrinth!!, //必须为非空处理
            modrinthVersion,
            *modrinthCategories,
            modrinthModLoader
        ),
        index = sortField,
        offset = index,
        limit = limit
    )
}