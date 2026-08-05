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

package tech.voltagestudios.dream.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import tech.voltagestudios.dream.game.control.DevMenuManager
import tech.voltagestudios.dream.setting.AllSettings

/**
 * ViewModel for the Dev Menu screen
 */
class DevMenuViewModel : ViewModel() {
    
    /**
     * Whether the dev mode bypass is currently enabled
     */
    var isDevModeEnabled by mutableStateOf(AllSettings.devModeBypass.getValue())
        private set
    
    /**
     * Whether the dev menu is currently visible
     */
    var isDevMenuVisible by mutableStateOf(false)
        private set
    
    init {
        // Observe setting changes
        AllSettings.devModeBypass.addListener {
            isDevModeEnabled = it
        }
    }
    
    /**
     * Toggle the dev mode bypass setting
     */
    fun toggleDevModeBypass() {
        val newValue = !isDevModeEnabled
        AllSettings.devModeBypass.save(newValue)
        isDevModeEnabled = newValue
    }
    
    /**
     * Show the dev menu
     */
    fun showDevMenu() {
        isDevMenuVisible = true
    }
    
    /**
     * Hide the dev menu
     */
    fun hideDevMenu() {
        isDevMenuVisible = false
    }
    
    /**
     * Toggle the dev menu visibility
     */
    fun toggleDevMenu() {
        isDevMenuVisible = !isDevMenuVisible
    }
    
    /**
     * Check if dev mode bypass is enabled
     */
    fun isBypassEnabled(): Boolean {
        return AllSettings.devModeBypass.getValue()
    }
    
    override fun onCleared() {
        super.onCleared()
        AllSettings.devModeBypass.removeListener()
    }
}
