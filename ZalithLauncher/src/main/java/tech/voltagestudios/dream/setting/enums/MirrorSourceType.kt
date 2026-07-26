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

package tech.voltagestudios.dream.setting.enums

import tech.voltagestudios.dream.R

enum class MirrorSourceType(val textRes: Int) {
    /**
     * 官方源优先
     */
    OFFICIAL_FIRST(R.string.settings_launcher_mirror_official_first),

    /**
     * 镜像源优先
     */
    MIRROR_FIRST(R.string.settings_launcher_mirror_mirror_first)
}