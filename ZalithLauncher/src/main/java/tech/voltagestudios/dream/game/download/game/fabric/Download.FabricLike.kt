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

package tech.voltagestudios.dream.game.download.game.fabric

import tech.voltagestudios.dream.coroutine.Task
import tech.voltagestudios.dream.game.addons.mirror.mapBMCLMirrorUrls
import tech.voltagestudios.dream.game.addons.modloader.fabriclike.FabricLikeVersion
import tech.voltagestudios.dream.utils.file.ensureParentDirectory
import tech.voltagestudios.dream.utils.network.fetchStringFromUrls
import kotlinx.coroutines.Dispatchers
import java.io.File

const val FABRIC_LIKE_DOWNLOAD_ID = "Download.FabricLike"

fun getFabricLikeDownloadTask(
    fabricLikeVersion: FabricLikeVersion,
    tempVersionJson: File
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = {
            //下载版本 Json
            val loaderJson = fetchStringFromUrls(fabricLikeVersion.loaderJsonUrl.mapBMCLMirrorUrls())
            tempVersionJson
                .ensureParentDirectory()
                .writeText(loaderJson)
        }
    )
}