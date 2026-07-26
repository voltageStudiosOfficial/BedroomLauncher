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

package tech.voltagestudios.dream.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.voltagestudios.dream.setting.enums.isLauncherInDarkTheme
import tech.voltagestudios.dream.ui.base.BaseScreen
import tech.voltagestudios.dream.ui.code_editor.SoraEditor
import tech.voltagestudios.dream.ui.code_editor.lang.MarkdownLanguage
import tech.voltagestudios.dream.ui.code_editor.scheme.SchemeIDEADark
import tech.voltagestudios.dream.ui.code_editor.scheme.SchemeIDEALight
import tech.voltagestudios.dream.ui.screens.NormalNavKey
import tech.voltagestudios.dream.viewmodel.LocalHomePageViewModel
import tech.voltagestudios.dream.viewmodel.ScreenBackStackViewModel

@Composable
fun HomePageEditorScreen(
    backStackViewModel: ScreenBackStackViewModel,
) {
    val homePageViewModel = LocalHomePageViewModel.current
    val editorState by homePageViewModel.editorState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isDark = isLauncherInDarkTheme()

    BaseScreen(
        screenKey = NormalNavKey.HomePageEditor,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val scheme = remember(isDark) {
                if (isDark) SchemeIDEADark() else SchemeIDEALight()
            }
            val language = remember { MarkdownLanguage(true) }

            SoraEditor(
                state = editorState,
                language = language,
                scheme = scheme,
                onSaveClick = {
                    homePageViewModel.localEditorSave(context)
                }
            )
        }
    }
}
