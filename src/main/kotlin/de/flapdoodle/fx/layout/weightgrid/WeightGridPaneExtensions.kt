package de.flapdoodle.fx.layout.weightgrid

import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node

fun <T : Node> T.withPosition(
        column: Int,
        row: Int,
        horizontalPosition: HPos? = null,
        verticalPosition: VPos? = null
): T {
    WeightGridPane.setPosition(this, column, row, horizontalPosition, verticalPosition)
    return this
}

fun <T : Node> T.updateRow(row: Int) {
    WeightGridPane.updatePosition(this) { it.copy(row = row) }
}
