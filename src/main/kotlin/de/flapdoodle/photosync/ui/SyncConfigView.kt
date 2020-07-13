package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.layout.weightgrid.WeightGridPane
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.bindFrom
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ActionEvent
import de.flapdoodle.photosync.ui.events.ModelEvent
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import tornadofx.*

class SyncConfigView(currentConfig: LazyValue<SyncConfig>) : Fragment("Sync Config") {

    companion object {
        const val SOURCE_COLUMN = 0
        const val DST_COLUMN = 1
        const val ACTION_COLUMN = 2
        const val SYNC_COLUMN = 3

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
            setColumnWeight(SOURCE_COLUMN, 10.0)
            setColumnWeight(DST_COLUMN, 10.0)
            setColumnWeight(ACTION_COLUMN, 1.0)
            setColumnWeight(SYNC_COLUMN, 1.0)

            children += Button("New")
                    .withPosition(ACTION_COLUMN, 0, horizontalPosition = HPos.CENTER)
                    .apply {
                        minWidth = Region.USE_PREF_SIZE
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

    class SyncConfigNodes(index: Int, var mapping: SyncEntry) {

        private val source = Label(mapping.src)
                .apply {
                    useMaxWidth = true
                }
                .withPosition(SOURCE_COLUMN, index, horizontalPosition = HPos.LEFT)
        private val dst = Label(mapping.dst)
                .apply {
                    useMaxWidth = true
                }
                .withPosition(DST_COLUMN, index, horizontalPosition = HPos.LEFT)
        private val delete = Button("delete")
                .withPosition(ACTION_COLUMN, index, horizontalPosition = HPos.CENTER)
                .apply {
                    minWidth = Region.USE_PREF_SIZE
                    action {
                        ModelEvent.deleteConfig(mapping.id).fire()
                    }
                }
        private val startSync = Button("sync")
                .withPosition(SYNC_COLUMN, index, horizontalPosition = HPos.CENTER)
                .apply {
                    subscribeEvent<ActionEvent> {event ->
                        when (event.action) {
                            is ActionEvent.Action.SyncFinished -> {
                                if (event.action.id == mapping.id) {
                                    text = "Done"
                                }
                            }
                        }
                    }
                    minWidth = Region.USE_PREF_SIZE
                    action {
                        ActionEvent.startSync(mapping.id).fire()
                    }
                }

        fun nodes(): List<Node> {
            return listOf(source, dst, delete, startSync);
        }

        fun update(index: Int, mapping: SyncEntry?) {
            require(mapping != null) { "mapping is null" }

            this.mapping = mapping
            source.text = mapping.src
            dst.text = mapping.dst

            source.updateRow(index)
            dst.updateRow(index)
            delete.updateRow(index)
            startSync.updateRow(index)
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