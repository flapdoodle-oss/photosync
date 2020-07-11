package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.layout.weightgrid.WeightGridPane
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.bindFrom
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class SyncConfigView(currentConfig: LazyValue<SyncConfig>) : Fragment("Sync Config") {

    private val configs = currentConfig.mapToList { it.entries }

    override val root = vbox {

        this += WeightGridPane().apply {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            
            style {
                padding = box(4.0.px)
                borderWidth += box(1.0.px)
                borderColor += box(Color.BLUE)
            }
            setColumnWeight(0, 1.0)
            setColumnWeight(1, 1.0)
            setColumnWeight(2, 0.0)

            children.bindFrom(configs,
                    keyOf = { it.id },
                    extract = SyncConfigNodes::nodes) { index, source, mapped ->
                mapped?.apply {
                    update(index, source)
                } ?: SyncConfigNodes.map(index, source)
            }
        }
    }

    class SyncConfigNodes(var index: Int, var mapping: SyncEntry) {

        private val source =  Label(mapping.src)
                .withPosition(0, index, horizontalPosition = HPos.LEFT)
        private val dst =  Label(mapping.dst)
                .withPosition(1, index, horizontalPosition = HPos.LEFT)
        private val delete =  Button("delete")
                .withPosition(2, index, horizontalPosition = HPos.LEFT)

        fun nodes(): List<Node> {
            return listOf(source, dst, delete);
        }

        fun update(index: Int, mapping: SyncEntry?) {
            source.text = mapping?.src ?: "?"
            dst.text = mapping?.dst ?: "?"
            
            source.updateRow(index);
            dst.updateRow(index);
            delete.updateRow(index);
        }

        companion object {
            internal fun <T : Node> T.updateRow(row: Int) {
                WeightGridPane.updatePosition(this) { it.copy(row = row) }
            }

            private fun <T : Node> T.withPosition(
                    column: Int,
                    row: Int,
                    horizontalPosition: HPos? = null,
                    verticalPosition: VPos? = null
            ): T {
                WeightGridPane.setPosition(this, column, row, horizontalPosition, verticalPosition)
                return this
            }

            fun map(
                    index: Int,
                    mapping: SyncEntry
            ): SyncConfigNodes {
                return SyncConfigNodes(index, mapping);
            }
        }
    }
}