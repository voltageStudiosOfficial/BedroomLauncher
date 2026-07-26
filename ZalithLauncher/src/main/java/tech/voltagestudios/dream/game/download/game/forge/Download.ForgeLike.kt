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

package tech.voltagestudios.dream.game.download.game.forge

import tech.voltagestudios.dream.coroutine.Task
import tech.voltagestudios.dream.game.addons.mirror.mapBMCLMirrorUrls
import tech.voltagestudios.dream.game.addons.modloader.forgelike.ForgeLikeVersion
import tech.voltagestudios.dream.game.addons.modloader.forgelike.forge.ForgeVersion
import tech.voltagestudios.dream.game.addons.modloader.forgelike.forge.ForgeVersions
import tech.voltagestudios.dream.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import tech.voltagestudios.dream.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import tech.voltagestudios.dream.utils.network.downloadFromMirrorListSuspend
import tech.voltagestudios.dream.utils.network.withSpeedReport
import java.io.File

const val FORGE_LIKE_DOWNLOAD_ID = "Download.ForgeLike"

fun targetTempForgeLikeInstaller(tempGameDir: File): File = File(tempGameDir, ".temp/forge_like_installer.jar")

/**
 * 判断是否为 NeoForge 版本
 */
val ForgeLikeVersion.isNeoForge: Boolean
    get() = this is NeoForgeVersion

fun getForgeLikeDownloadTask(
    targetTempInstaller: File,
    forgeLikeVersion: ForgeLikeVersion
): Task {
    return Task.runTask(
        id = FORGE_LIKE_DOWNLOAD_ID,
        task = { task ->
            //获取安装器下载链接
            val url = if (forgeLikeVersion.isNeoForge) {
                NeoForgeVersions.getDownloadUrl(forgeLikeVersion as NeoForgeVersion)
            } else {
                ForgeVersions.getDownloadUrl(forgeLikeVersion as ForgeVersion)
            }

            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFromMirrorListSuspend(
                    urls = url.mapBMCLMirrorUrls(),
                    outputFile = targetTempInstaller,
                    sizeCallback = report
                )
            }
        }
    )
}