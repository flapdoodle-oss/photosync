package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.layout.weightgrid.WeightGridPane
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.bindFrom
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class SyncConfigView(currentConfig: LazyValue<SyncConfig>) : Fragment("Sync Config") {

    companion object {
        const val SOURCE_COLUMN=0
        const val DST_COLUMN=1
        const val ACTION_COLUMN=2

        private fun <T : Node> T.withPosition(
                column: Int,
                row: Int,
                horizontalPosition: HPos? = null,
                verticalPosition: VPos? = null
        ): T {
            WeightGridPane.setPosition(this, column, row, horizontalPosition, verticalPosition)
            return this
        }
    }

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
            setColumnWeight(SOURCE_COLUMN, 1.0)
            setColumnWeight(DST_COLUMN, 1.0)
            setColumnWeight(ACTION_COLUMN, 0.0)

            children += Button("New")
                    .withPosition(ACTION_COLUMN, 0, horizontalPosition = HPos.CENTER)
                    .apply {
                        action {
                            AddSyncConfigView.openModal()
                        }
                    }

            children.bindFrom(configs,
                    keyOf = { it.id },
                    extract = SyncConfigNodes::nodes) { index, source, mapped ->
                mapped?.apply {
                    update(index + 1, source)
                } ?: SyncConfigNodes.map(index + 1, source)
            }
        }
    }

    class SyncConfigNodes(var index: Int, var mapping: SyncEntry) {

        private val source =  Label(mapping.src)
                .withPosition(SOURCE_COLUMN, index, horizontalPosition = HPos.LEFT)
        private val dst =  Label(mapping.dst)
                .withPosition(DST_COLUMN, index, horizontalPosition = HPos.LEFT)
        private val delete =  Button("delete")
                .withPosition(ACTION_COLUMN, index, horizontalPosition = HPos.CENTER)

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

            fun map(
                    index: Int,
                    mapping: SyncEntry
            ): SyncConfigNodes {
                return SyncConfigNodes(index, mapping);
            }
        }
    }
}