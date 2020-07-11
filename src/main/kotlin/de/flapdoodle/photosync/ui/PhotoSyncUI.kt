package de.flapdoodle.photosync.ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import tornadofx.*


class PhotoSyncUI : App(StartView::class) {

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