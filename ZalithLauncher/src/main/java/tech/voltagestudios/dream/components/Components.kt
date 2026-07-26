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

package tech.voltagestudios.dream.components

import tech.voltagestudios.dream.R

enum class Components(val component: String, val displayName: String, val summary: Int) {
    AUTH_LIBS("auth_libs", "authlib-injector", R.string.unpack_screen_authlib_injector),
    CACIOCAVALLO("caciocavallo", "caciocavallo", R.string.unpack_screen_cacio),
    CACIOCAVALLO17("caciocavallo17", "caciocavallo 17", R.string.unpack_screen_cacio),
    LWJGL3("lwjgl3", "LWJGL 3.3.6", R.string.unpack_screen_lwjgl),
    LAUNCHER("launcher", "Launcher Components", R.string.unpack_screen_launcher)
}