package de.flapdoodle.dirsync.ui

import de.flapdoodle.dirsync.ui.views.StartView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import tornadofx.App
import tornadofx.FX

class DirSyncUI : App(StartView::class) {

    init {
        FX.layoutDebuggerShortcut = KeyCodeCombination(
                KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN
        )
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            tornadofx.launch<DirSyncUI>(*args)
        }
    }
}