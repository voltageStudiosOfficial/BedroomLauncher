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

package tech.voltagestudios.dream.game.control

import android.view.KeyEvent
import tech.voltagestudios.dream.setting.AllSettings
import tech.voltagestudios.dream.ui.control.gamepad.DpadDirection
import tech.voltagestudios.dream.utils.logging.Logger

/**
 * Manages the Konami code detection for accessing the dev menu.
 * Konami code: UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, B, A
 */
object DevMenuManager {
    private const val TAG = "DevMenuManager"
    
    // Konami code sequence: UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, B, A
    private val konamiSequence = listOf(
        DpadDirection.Up,
        DpadDirection.Up,
        DpadDirection.Down,
        DpadDirection.Down,
        DpadDirection.Left,
        DpadDirection.Right,
        DpadDirection.Left,
        DpadDirection.Right,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_A
    )
    
    private val inputBuffer = mutableListOf<Any>()
    private const val INPUT_TIMEOUT_MS = 5000L // 5 seconds to complete the code
    private var lastInputTime = 0L
    
    /**
     * Called when a D-pad direction is pressed
     */
    fun onDpadInput(direction: DpadDirection, pressed: Boolean) {
        if (!pressed) return
        
        val now = System.currentTimeMillis()
        
        // Reset if timeout exceeded
        if (now - lastInputTime > INPUT_TIMEOUT_MS) {
            inputBuffer.clear()
        }
        
        inputBuffer.add(direction)
        lastInputTime = now
        
        checkKonamiCode()
    }
    
    /**
     * Called when a gamepad button is pressed
     */
    fun onButtonInput(keyCode: Int, pressed: Boolean) {
        if (!pressed) return
        
        val now = System.currentTimeMillis()
        
        // Reset if timeout exceeded
        if (now - lastInputTime > INPUT_TIMEOUT_MS) {
            inputBuffer.clear()
        }
        
        inputBuffer.add(keyCode)
        lastInputTime = now
        
        checkKonamiCode()
    }
    
    /**
     * Check if the input buffer matches the Konami code
     */
    private fun checkKonamiCode() {
        if (inputBuffer.size < konamiSequence.size) return
        
        val lastInputs = inputBuffer.takeLast(konamiSequence.size)
        
        val matches = lastInputs.zip(konamiSequence).all { (input, expected) ->
            when (expected) {
                is DpadDirection -> input == expected
                is Int -> input == expected
                else -> false
            }
        }
        
        if (matches) {
            Logger.debug(TAG, "Konami code activated!")
            inputBuffer.clear()
            onDevMenuActivated()
        }
    }
    
    /**
     * Reset the input buffer
     */
    fun reset() {
        inputBuffer.clear()
        lastInputTime = 0L
    }
    
    /**
     * Callback when dev menu is activated
     */
    private var onActivatedCallback: (() -> Unit)? = null
    
    fun setOnDevMenuActivated(callback: () -> Unit) {
        onActivatedCallback = callback
    }
    
    private fun onDevMenuActivated() {
        onActivatedCallback?.invoke()
    }
    
    /**
     * Check if dev menu is currently enabled (bypass setting)
     */
    fun isDevModeEnabled(): Boolean {
        return AllSettings.devModeBypass.getValue()
    }
    
    /**
     * Enable/disable dev mode bypass
     */
    fun setDevModeEnabled(enabled: Boolean) {
        AllSettings.devModeBypass.save(enabled)
    }
}
