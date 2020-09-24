package de.flapdoodle.dirsync.ui.views

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.dirsync.ui.config.SyncEntry
import de.flapdoodle.dirsync.ui.events.ActionEvent
import de.flapdoodle.dirsync.ui.events.ModelEvent
import de.flapdoodle.fx.extensions.*
import de.flapdoodle.fx.layout.weightflex.Range
import de.flapdoodle.fx.layout.weightflex.WeightFlexPane
import de.flapdoodle.fx.layout.weightflex.updateRow
import de.flapdoodle.fx.layout.weightflex.withPosition
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.SyncConfigView
import javafx.geometry.HPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import tornadofx.*

class SyncConfigFragment(val currentConfig: LazyValue<SyncConfig>) : Fragment("Sync Config") {
    private val entries = currentConfig.mapToList { it.entries }

    companion object {
        const val SOURCE_COLUMN = 0
        const val DST_COLUMN = 1
        const val ACTION_COLUMN = 2
        const val CONFIG_COLUMN = 3
        const val SYNC_COLUMN = 4
    }

    override val root = vbox {
        this += WeightFlexPane().apply {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            setColumnWeight(SOURCE_COLUMN, 10.0)
            setColumnWeight(DST_COLUMN, 10.0)
            setColumnWeight(ACTION_COLUMN, 1.0)
            setColumnWeight(CONFIG_COLUMN, 1.0)
            setColumnWeight(SYNC_COLUMN, 1.0)

            style {
                padding = box(4.0.px)
                borderWidth += box(1.0.px)
                borderColor += box(Color.BLUE)
            }

//            spacer {
//                withPosition(SOURCE_COLUMN, 0)
//                style {
//                    borderWidth += box(1.0.px)
//                    borderColor += box(Color.RED)
//                }
//            }
            label {
                withPosition(SOURCE_COLUMN, 0)
                useMaxWidth = true
            }
            label {
                withPosition(DST_COLUMN, 0)
                useMaxWidth = true
            }
            button("+") {
                withPosition(ACTION_COLUMN, 0, horizontalPosition = HPos.CENTER)
                minWidth = Region.USE_PREF_SIZE
                action {
                    AddSyncConfigView.openModal()
                }
            }
            label {
                withPosition(CONFIG_COLUMN, 0)
                useMaxWidth = true
            }
            label {
                withPosition(SYNC_COLUMN, 0)
                useMaxWidth = true
            }


            children.bindFromFactory(entries, SyncEntry::id, Factory(1))
        }
    }

    class Factory(val offset: Int) : LazyNodeFactory<SyncEntry, Container> {

        override fun create(index: Int, source: SyncEntry, old: Container?): Container {
            return old?.update(index + offset, source) ?: Container(index + offset, source)
        }

    }

    class Container(index: Int, entry: SyncEntry) : LazyNodeContainer<SyncEntry> {
        private val source = Label(entry.src)
                .apply {
                    useMaxWidth = true
                }.withPosition(SOURCE_COLUMN, index, horizontalPosition = HPos.LEFT)
        private val dst = Label(entry.dst)
                .apply {
                    useMaxWidth = true
                }.withPosition(DST_COLUMN, index, horizontalPosition = HPos.LEFT)

        private val delete = Button("❌")
                .apply {
                    minWidth = Region.USE_PREF_SIZE
                    action {
                        ModelEvent.deleteConfig(entry.id)
                    }
                }.withPosition(ACTION_COLUMN, index)

        private val edit = Button("✎")
                .apply {
                    minWidth = Region.USE_PREF_SIZE
                    action {
                        AddSyncConfigView.openModal(entry)
                    }
                }.withPosition(CONFIG_COLUMN, index)

        private val sync = Button("⇔")
                .apply {
                    minWidth = Region.USE_PREF_SIZE
                    action {
                        ActionEvent.startScan(entry.id).fire()
                        isDisable = true
                    }
                    subscribeEvent<ActionEvent> { event ->
                        when (event.action) {
                            is ActionEvent.Action.ScanFinished -> isDisable = false
                            is ActionEvent.Action.ScanAborted -> isDisable = false
                        }
                    }
                }.withPosition(SYNC_COLUMN, index)

        override fun nodes(): List<Node> {
            return listOf(source, dst, delete, edit, sync)
        }

        fun update(index: Int, source: SyncEntry): Container {
            nodes().forEach { it.updateRow(Range.of(index)) }
            return this
        }

    }
}
