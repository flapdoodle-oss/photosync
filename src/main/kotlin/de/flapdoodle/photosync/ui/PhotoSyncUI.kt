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


class PhotoSyncUI : App(Hello::class) {

  init {
    FX.layoutDebuggerShortcut = KeyCodeCombination(
        KeyCode.D,KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN
    )
  }

//  override fun start(stage: Stage) {
//    val scene = Scene(StackPane(Label("what")), 640.0, 480.0)
//    stage.scene = scene
//    stage.show()
//  }
}