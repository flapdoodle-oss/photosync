package de.flapdoodle.fx.layout.splitpane

import javafx.scene.control.Label
import tornadofx.*

class BetterSplitPaneSampler : View("Better Split Pane") {
  override val root = borderpane {
    center {
//      style {
//        borderWidth += box(1.0.px)
//        borderColor += box(Color.RED)
//      }
      this += BetterSplitPane().apply {
        add(Label("one"))
        add(Label("two").apply {
          maxWidth = 200.0
        })
        add(Label("3").apply {
          maxWidth = 100.0
        })
      }
    }
    bottom {
      splitpane {
        label("one")
        label("two") {
          maxWidth = 200.0
        }
        label("3") {
          maxWidth = 100.0
        }
      }
    }
  }

  companion object {
    // put instance creation here
    fun open() {
      val view = find(BetterSplitPaneSampler::class)
      view.openWindow(stageStyle = javafx.stage.StageStyle.DECORATED)
    }
  }
}
