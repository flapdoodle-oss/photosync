package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.fx.layout.AutoArray
import de.flapdoodle.fx.layout.GridMap
import de.flapdoodle.fx.extensions.constraint
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.SimpleStyleableDoubleProperty
import javafx.css.Styleable
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Control
import tornadofx.*

class WeightFlexPane : Control() {

  internal val horizontalSpace = object : SimpleStyleableDoubleProperty(WeightFlexPaneStyle.CSS_HSPACE, this, "hspace") {
    override fun invalidated() {
      requestLayout()
    }
  }

  internal val verticalSpace = object : SimpleStyleableDoubleProperty(WeightFlexPaneStyle.CSS_VSPACE, this, "vspace") {
    override fun invalidated() {
      requestLayout()
    }
  }

  internal var rowWeights = AutoArray.empty<Double>()
  internal var columnWeights = AutoArray.empty<Double>()

  init {
    addClass(WeightFlexPaneStyle.clazz)
    stylesheets += WeightFlexPaneStyle().base64URL.toExternalForm()
  }

  companion object {
    fun setPosition(
        node: Node,
        column: Int,
        row: Int,
        horizontalPosition: HPos? = null,
        verticalPosition: VPos? = null
    ) {
      node.constraint[Area::class] = Area.of(column, row)
      node.constraint[HPos::class] = horizontalPosition
      node.constraint[VPos::class] = verticalPosition
    }

    fun updatePosition(
        node: Node,
        change: (Area) -> Area
    ) {
      val current = node.constraint[Area::class]
      require(current != null) { "no position found for $node" }
      node.constraint[Area::class] = change(current)
    }

//    @JvmStatic
//    fun getClassCssMetaData(): MutableList<CssMetaData<out Styleable, *>?> {
//      return Skin.ALL.toMutableList()
//    }
  }

  private val skin = WeightFlexPaneSkin(this)
  override fun createDefaultSkin() = skin

  fun setRowWeight(row: Int, weight: Double) {
    require(row >= 0) { "invalid row: $row" }
    require(weight >= 0.0) { "invalid weight: $weight" }

    rowWeights = rowWeights.set(row, weight)

    requestLayout()
  }

  fun setColumnWeight(column: Int, weight: Double) {
    require(column >= 0) { "invalid column: $column" }
    require(weight >= 0.0) { "invalid weight: $weight" }

    columnWeights = columnWeights.set(column, weight)

    requestLayout()
  }

  fun horizontalSpaceProperty() = horizontalSpace
  fun verticalSpaceProperty() = verticalSpace

//  override fun getUserAgentStylesheet(): String {
//    //return Style().base64URL.toExternalForm()
//    return stylesheets.joinToString(separator = ";") + Style().base64URL.toExternalForm()
//  }

  public override fun getChildren(): ObservableList<Node> {
    return super.getChildren()
  }

  override fun getControlCssMetaData(): List<CssMetaData<out Styleable, *>> {
    return WeightFlexPaneStyle.CONTROL_CSS_META_DATA
  }
}