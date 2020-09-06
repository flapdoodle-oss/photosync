package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.fx.layout.weightgrid.WeightGridPane
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node

fun <T : Node> T.withPosition(
        column: Range,
        row: Range,
        horizontalPosition: HPos? = null,
        verticalPosition: VPos? = null
): T {
    WeightFlexPane.setPosition(this, Area.of(column, row), horizontalPosition, verticalPosition)
    return this
}

fun <T : Node> T.updateRow(row: Range) {
    WeightFlexPane.updatePosition(this) { it.copy(row = row) }
}
