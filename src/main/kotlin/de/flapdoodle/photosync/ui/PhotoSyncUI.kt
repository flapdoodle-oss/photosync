package de.flapdoodle.photosync.ui

import de.flapdoodle.photosync.ui.sync.LabelStyles
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import tornadofx.App
import tornadofx.FX
import tornadofx.launch


class PhotoSyncUI : App(StartView::class, LabelStyles::class) {

  init {
    FX.layoutDebuggerShortcut = KeyCodeCombination(
        KeyCode.D,KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN
    )
  }

  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      launch<PhotoSyncUI>(*args)
    }
  }
}