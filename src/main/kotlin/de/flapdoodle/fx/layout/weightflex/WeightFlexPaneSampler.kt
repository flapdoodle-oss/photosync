package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.sync.SyncCommand
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncGroupCommandsFactory
import javafx.geometry.HPos
import tornadofx.*
import java.nio.file.Path

class WeightFlexPaneSampler : View("Weighted Grid Pane") {
  override val root = borderpane {
    top {
      if (true) {
        this += WeightFlexPane().apply {
          setColumnWeight(0, 1.0)
          setColumnWeight(1, 4.0)
          setColumnWeight(2, 1.0)

          label("label") {
            WeightFlexPane.setPosition(this, 0, 1)
          }
          textfield("text") {
            WeightFlexPane.setPosition(this, 1, 1)
          }
          button("change") {
            WeightFlexPane.setPosition(this, 2, 1)
            action {
              println("v=${horizontalSpaceProperty().value}")
              horizontalSpaceProperty().value = 10.0
            }
          }
          label("... all ...") {
            WeightFlexPane.setPosition(this, Area.of(Range.of(0, 2), Range.of(0)), horizontalPosition = HPos.CENTER)
          }
        }
      }

      val sample: List<SyncGroup> = listOf(SyncGroup(commands = listOf(
              SyncGroup.SyncEntry(SyncCommand.Copy(Path.of("src"), Path.of("dst")),SyncGroup.Status.NotExcuted),
              SyncGroup.SyncEntry(SyncCommand.Copy(Path.of("a"), Path.of("b")),SyncGroup.Status.Failed)
      )))
      this += SyncGroupCommandsFactory.render(ChangeableValue(sample))
    }
    center {
//      style {
//        borderWidth += box(1.0.px)
//        borderColor += box(Color.RED)
//      }
      if (true) {
        this += WeightFlexPane().apply {
          stylesheet {
            WeightFlexPaneStyle.clazz {
              WeightFlexPaneStyle.horizontalSpace.value = 10.0
              WeightFlexPaneStyle.verticalSpace.value = 10.0
            }
          }
          button("test") {
            minWidth = 20.0
            maxWidth = 100.0
            WeightFlexPane.setPosition(this, 0, 0)
          }
          button("test-1") {
            WeightFlexPane.setPosition(this, 1, 0, horizontalPosition = HPos.RIGHT)
          }
          button("test-11") {
            WeightFlexPane.setPosition(this, 1, 1)
            maxHeight = 100.0
          }

          setColumnWeight(0, 1.0)
          setColumnWeight(1, 2.0)
          setRowWeight(0, 4.0)
          setRowWeight(1, 1.0)
        }
      }
    }
    if (true) {
      bottom {
        this += WeightFlexPane().apply {
          setColumnWeight(0, 1.0)
          setColumnWeight(1, 4.0)
          setColumnWeight(2, 1.0)

          label("label") {
            WeightFlexPane.setPosition(this, 0, 0)
          }
          textfield("text") {
            WeightFlexPane.setPosition(this, 1, 0)
          }
          button("change") {
            WeightFlexPane.setPosition(this, 2, 0)
            action {
              println("v=${horizontalSpaceProperty().value}")
              horizontalSpaceProperty().value = 10.0
            }
          }
          label("s") {
            WeightFlexPane.setPosition(this, 0, 1, horizontalPosition = HPos.LEFT)
          }
          textfield("text") {
            WeightFlexPane.setPosition(this, 1, 1)
          }
          button("change") {
            WeightFlexPane.setPosition(this, 2, 1)
            action {
              println("v=${horizontalSpaceProperty().value}")
              horizontalSpaceProperty().value = 10.0
            }
          }
        }
      }
    }
  }

  companion object {
    // put instance creation here
    fun open() {
      val view = find(WeightFlexPaneSampler::class)
      view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
    }
  }
}
