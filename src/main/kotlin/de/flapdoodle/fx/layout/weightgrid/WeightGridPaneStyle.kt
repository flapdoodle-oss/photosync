package de.flapdoodle.fx.layout.weightgrid

import javafx.css.CssMetaData
import javafx.css.StyleablePropertyFactory
import javafx.scene.control.Control
import tornadofx.*

class WeightGridPaneStyle : Stylesheet() {
  companion object {
    internal val CSS_HSPACE_NAME = "weighted-grid-horizontal-space"
    internal val CSS_VSPACE_NAME = "weighted-grid-vertical-space"

    //      @JvmField
    private val FACTORY = StyleablePropertyFactory<WeightGridPane>(Control.getClassCssMetaData())

    //      @JvmField
    internal val CSS_HSPACE: CssMetaData<WeightGridPane, Number> = FACTORY.createSizeCssMetaData(
        CSS_HSPACE_NAME,
        { it.horizontalSpace },
        2.0)

    internal val CSS_VSPACE: CssMetaData<WeightGridPane, Number> = FACTORY.createSizeCssMetaData(
        CSS_VSPACE_NAME,
        { it.verticalSpace },
        2.0)

    //      @JvmStatic
    internal val CONTROL_CSS_META_DATA = (FACTORY.cssMetaData + CSS_HSPACE + CSS_VSPACE)

    val clazz by cssclass("weight-grid-pane")
    val horizontalSpace by cssproperty<Double>(CSS_HSPACE_NAME)
    val verticalSpace by cssproperty<Double>(CSS_VSPACE_NAME)
  }

  init {
    clazz {
      horizontalSpace.value = 4.0
      verticalSpace.value = 4.0
    }
  }
}