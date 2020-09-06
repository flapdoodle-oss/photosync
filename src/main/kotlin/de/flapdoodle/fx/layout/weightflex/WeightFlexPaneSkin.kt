package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.fx.extensions.*
import de.flapdoodle.fx.layout.GridMap
import de.flapdoodle.fx.layout.WeightedSize
import javafx.collections.ListChangeListener
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.SkinBase

class WeightFlexPaneSkin(
        private val control: WeightFlexPane
) : SkinBase<WeightFlexPane>(control) {

    private var flexMap: FlexMap<Node> = FlexMap()

    init {
        children.addListener(ListChangeListener {
            flexMap = gridMap()
            updateState()
        })

        control.needsLayoutProperty().addListener { observable, oldValue, newValue ->
            flexMap = gridMap()
        }
    }

    private fun gridMap(): FlexMap<Node> {
        return FlexMap(children
                .filter { it.isManaged }
                .map { it: Node ->
                    it to (it.constraint[Area::class]
                            ?: Area.of(0, 0))
                }.toMap())
    }

    private fun updateState() {
        control.requestLayout()
    }

    private fun verticalSpace(): Double = control.verticalSpace.value
    private fun horizontalSpace(): Double = control.horizontalSpace.value

    private fun <T : Any> Map<Int, T>.subMap(start: Int, end: Int): Map<Int, T> {
        return this.filterKeys { it -> start <= it && it < end }
    }

    private fun <T : Any> Map<*, T>.sumWithSpaceBetween(space: Double, selector: (T) -> Double): Double {
        return values.sumByDouble(selector) + if (isEmpty()) 0.0 else (size - 1) * space
    }

    private fun <T : Any> Map<*, T>.sumWithSpaceAfter(space: Double, selector: (T) -> Double): Double {
        return values.sumByDouble(selector) + size * space
    }

    private fun columnSizes() = flexMap.columnSizes(
            columnWeight = { it -> Weight(control.columnWeights.get(it) ?: 1.0) },
            limitsOf = Node::widthDimensions
    )

    private fun rowSizes() = flexMap.rowSizes(
            rowWeight = { it -> Weight(control.rowWeights.get(it) ?: 1.0) },
            limitsOf = Node::heightDimensions
    )
//  private fun columnSizes() = flexMap.mapColumns { index, list ->
//    val limits = list.map { it.widthLimits() }
//    val min = limits.map { it.first }.max() ?: 0.0
//    val max = Math.max(min, limits.map { it.second }.max() ?: Double.MAX_VALUE)
//
////      require(max >= min) { "invalid min/max for $list -> $min ? $max" }
//    WeightedSize(control.columnWeights.get(index) ?: 1.0, min, max)
//  }
//
//
//  private fun rowSizes() = flexMap.mapRows { index, list ->
//    val limits = list.map { it.heightLimits() }
//    val min = limits.map { it.first }.max() ?: 0.0
//    val max = Math.max(min, limits.map { it.second }.max() ?: Double.MAX_VALUE)
//
////      require(max >= min) { "invalid min/max for $list -> $min ? $max" }
//    WeightedSize(control.columnWeights.get(index) ?: 1.0, min, max)
//  }

    override fun computeMinWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val width = columnSizes().sumWithSpaceBetween(horizontalSpace()) { it.min }
        return width + leftInset + rightInset
    }

    override fun computeMinHeight(width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val ret = rowSizes().sumWithSpaceBetween(verticalSpace()) { it.min }
        return ret + topInset + bottomInset
    }

    override fun computeMaxWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val width = columnSizes().sumWithSpaceBetween(horizontalSpace()) { it.max }
        return width + leftInset + rightInset
    }

    override fun computeMaxHeight(width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val ret = rowSizes().sumWithSpaceBetween(verticalSpace()) { it.max }
        return ret + topInset + bottomInset
    }

    override fun computePrefWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val ret = columnSizes().sumWithSpaceBetween(horizontalSpace()) { it.prefered }
//    val ret = flexMap.mapColumns { _, list ->
//      list.map { it.prefWidth(-1.0) }.max() ?: 0.0
//    }.sumWithSpaceBetween(horizontalSpace()) { it }
        return ret + leftInset + rightInset
    }

    override fun computePrefHeight(width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        val ret = rowSizes().sumWithSpaceBetween(verticalSpace()) { it.prefered }
//    val ret = flexMap.mapRows { _, list ->
//      list.map { it.prefHeight(-1.0) }.max() ?: 0.0
//    }.sumWithSpaceBetween(verticalSpace()) { it }
        return ret + topInset + bottomInset
    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
//        println("-------------------------")

//      println("hspace: ${horizontalSpace.value}")
        val columnSizes = columnSizes()
        val rowSizes = rowSizes()

        val hSpaces = if (columnSizes.isEmpty()) 0.0 else (columnSizes.size - 1) * horizontalSpace()
        val vSpaces = if (rowSizes.isEmpty()) 0.0 else (rowSizes.size - 1) * verticalSpace()

//        println("columns")
//        columnSizes.forEach { println(it) }
//        println("rows")
//        rowSizes.forEach { println(it) }

        val colWidths = WeightedSizeCalculator.distribute(contentWidth - hSpaces, columnSizes)
        val rowHeights = WeightedSizeCalculator.distribute(contentHeight - vSpaces, rowSizes)

//        println("widths: $colWidths")
//        println("heights: $rowHeights")
//        println("-------------------------")

//        println("container ($contentX x $contentY -> $contentWidth, $contentHeight)")
        flexMap.forEach { node, area ->
            val c_idx = area.column.start
            val r_idx = area.row.start

            if (node.isManaged) {

                val areaX = contentX + colWidths.subMap(0, c_idx).sumWithSpaceAfter(horizontalSpace()) { it }
                val areaY = contentY + rowHeights.subMap(0, r_idx).sumWithSpaceAfter(verticalSpace()) { it }

                val areaW = colWidths.subMap(c_idx, area.column.end+1).sumWithSpaceBetween(horizontalSpace()) { it }
                val areaH = rowHeights.subMap(r_idx, area.row.end+1).sumWithSpaceBetween(verticalSpace()) { it }

                val hPos = node.constraint[HPos::class] ?: HPos.CENTER
                val vPos = node.constraint[VPos::class] ?: VPos.CENTER

//                println("-----")
//                println("node: $node -> $area")
//                println("($areaX x $areaY -> $areaW, $areaH)")
                layoutInArea(node, areaX, areaY, areaW, areaH, -1.0, hPos, vPos)
            }
        }
//    flexMap.rows().forEachIndexed { r_idx, r ->
//      flexMap.columns().forEachIndexed { c_idx, c ->
//        val node = flexMap[GridMap.Pos(c, r)]
//        if (node != null && node.isManaged) {
//          val areaX = contentX + colWidths.subList(0, c_idx).sumWithSpaceAfter(horizontalSpace()) { it }
//          val areaY = contentY + rowHeights.subList(0, r_idx).sumWithSpaceAfter(verticalSpace()) { it }
//
//          val areaW = colWidths[c_idx]
//          val areaH = rowHeights[r_idx]
//
//          val hPos = node.constraint[HPos::class] ?: HPos.CENTER
//          val vPos = node.constraint[VPos::class] ?: VPos.CENTER
//
//          layoutInArea(node, areaX, areaY, areaW, areaH, -1.0, hPos, vPos)
//        }
//      }
//    }
    }
}