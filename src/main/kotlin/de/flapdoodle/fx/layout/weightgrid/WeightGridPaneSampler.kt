package de.flapdoodle.fx.layout.weightgrid

import javafx.geometry.HPos
import tornadofx.*

class WeightGridPaneSampler : View("Weighted Grid Pane") {
  override val root = borderpane {
    center {
//      style {
//        borderWidth += box(1.0.px)
//        borderColor += box(Color.RED)
//      }
      if (true) {
        this += WeightGridPane().apply {
          stylesheet {
            WeightGridPaneStyle.clazz {
              WeightGridPaneStyle.horizontalSpace.value = 10.0
              WeightGridPaneStyle.verticalSpace.value = 10.0
            }
          }
          button("test") {
            minWidth = 20.0
            maxWidth = 100.0
            WeightGridPane.setPosition(this, 0, 0)
          }
          button("test-1") {
            WeightGridPane.setPosition(this, 1, 0, horizontalPosition = HPos.RIGHT)
          }
          button("test-11") {
            WeightGridPane.setPosition(this, 1, 1)
            maxHeight = 100.0
          }

          setColumnWeight(0, 1.0)
          setColumnWeight(1, 2.0)
          setRowWeight(0, 4.0)
          setRowWeight(1, 1.0)
        }
      }

    }
    bottom {
      this += WeightGridPane().apply {
        setColumnWeight(0,1.0)
        setColumnWeight(1,4.0)
        setColumnWeight(2,1.0)

        label("label") {
          WeightGridPane.setPosition(this,0,0)
        }
        textfield("text") {
          WeightGridPane.setPosition(this,1,0)
        }
        button("change") {
          WeightGridPane.setPosition(this,2,0)
          action {
            println("v=${horizontalSpaceProperty().value}")
            horizontalSpaceProperty().value = 10.0
          }
        }
        label("s") {
          WeightGridPane.setPosition(this,0,1, horizontalPosition = HPos.LEFT)
        }
        textfield("text") {
          WeightGridPane.setPosition(this,1,1)
        }
        button("change") {
          WeightGridPane.setPosition(this,2,1)
          action {
            println("v=${horizontalSpaceProperty().value}")
            horizontalSpaceProperty().value = 10.0
          }
        }
      }
    }
  }

  companion object {
    // put instance creation here
    fun open() {
      val view = find(WeightGridPaneSampler::class)
      view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
    }
  }
}
