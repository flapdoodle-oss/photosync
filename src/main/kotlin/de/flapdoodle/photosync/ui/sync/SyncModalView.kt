package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.LazyNodeContainer
import de.flapdoodle.fx.extensions.LazyNodeFactory
import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.layout.weightgrid.WeightGridPane
import de.flapdoodle.fx.layout.weightgrid.updateRow
import de.flapdoodle.fx.layout.weightgrid.withPosition
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import de.flapdoodle.photosync.sync.SyncCommand
import de.flapdoodle.photosync.ui.components.StretchedLabel
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.Duration
import java.time.LocalDateTime

class SyncModalView : View("Sync") {
    private val result = ChangeableValue(SyncList(
            groups = emptyList(),
            dstDiskSpaceUsed = 0L,
            srcDiskSpaceUsed = 0L,
            start = LocalDateTime.now(),
            end = LocalDateTime.now()
    ))

    private val timeUsedInSeconds = result.map { "Scanned in ${Duration.between(it.start, it.end).toSeconds()}s" }

    private val syncCommandGroups = result.map { it.groups }

    override val root = borderpane {
        top {
            hbox {
                label(timeUsedInSeconds.asBinding())
                label(result.map {
                    "Diskspace used: ${it.srcDiskSpaceUsed / (1024 * 1024)} MB - ${it.dstDiskSpaceUsed / (1024 * 1024)} MB"
                }.asBinding())
            }
        }
        center {
            vbox {
                children.bindFromFactory(syncCommandGroups, SyncGroup::id, SyncGroupFactory())
            }
        }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.width = 800.0
            it.height = 600.0
        }
    }

    companion object {
        fun openModalWith(result: SyncList) {
            val view = find(SyncModalView::class)
            view.result.value(result)
            view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
        }
    }

    class SyncCommandsFactory : LazyNodeFactory<SyncGroup.SyncEntry, SyncCommandsFactory.Container> {


        class Container(row: Int, entry: SyncGroup.SyncEntry) : LazyNodeContainer<SyncGroup.SyncEntry> {
            private val current = ChangeableValue(entry);

            private val commandType = Label().apply {
                textProperty().bind(current.map { it.command.javaClass.simpleName ?: "?" }.asBinding())
            }.withPosition(NAME_COLUMN, row)

            private val src = StretchedLabel(current.map { sourceOf(it.command) }.asBinding())
                    .root.withPosition(SRC_COLUMN, row)

            private val dst = StretchedLabel(current.map { destinationOf(it.command) }.asBinding())
                    .root.withPosition(DST_COLUMN, row)

            private val hint = StretchedLabel(current.map { hintOf(it.command) }.asBinding())
                    .root.withPosition(HINT_COLUMN, row)

            private fun sourceOf(command: SyncCommand): String {
                return when(command) {
                    is SyncCommand.Move -> command.src.toString()
                    is SyncCommand.Remove -> ""
                    is SyncCommand.Copy -> command.src.toString()
                    is SyncCommand.CopyBack -> command.src.toString()
                }
            }

            private fun destinationOf(command: SyncCommand): String {
                return when(command) {
                    is SyncCommand.Move -> command.dst.toString()
                    is SyncCommand.Remove -> command.dst.toString()
                    is SyncCommand.Copy -> command.dst.toString()
                    is SyncCommand.CopyBack -> command.dst.toString()
                }
            }
            private fun hintOf(command: SyncCommand): String {
                return when(command) {
                    is SyncCommand.Move -> ""
                    is SyncCommand.Remove -> command.cause.toString()
                    is SyncCommand.Copy -> if (command.sameContent) "Same Content" else ""
                    is SyncCommand.CopyBack -> if (command.sameContent) "Same Content" else ""
                }
            }

            override fun nodes(): List<Node> {
                return listOf(commandType, src, dst, hint)
            }

            fun update(index: Int, source: SyncGroup.SyncEntry) {
                current.value(source)
                nodes().forEach { it.updateRow(index) }
            }
        }

        override fun create(index: Int, source: SyncGroup.SyncEntry, old: Container?): Container {
            return old?.apply {
                this.update(index, source)
            } ?: Container(index, source)
        }

        companion object {
            const val NAME_COLUMN = 0
            const val SRC_COLUMN = 1
            const val DST_COLUMN = 2
            const val HINT_COLUMN = 3

            fun render(src: LazyValue<List<SyncGroup.SyncEntry>>): WeightGridPane {
                return WeightGridPane().apply {
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS

                    style {
                        padding = box(4.0.px)
                        borderWidth += box(1.0.px)
                        borderColor += box(Color.BLUE)
                    }
                    setColumnWeight(NAME_COLUMN, 1.0)
                    setColumnWeight(SRC_COLUMN, 10.0)
                    setColumnWeight(DST_COLUMN, 10.0)
                    setColumnWeight(HINT_COLUMN, 1.0)

                    children.bindFromFactory(src, SyncGroup.SyncEntry::command, SyncCommandsFactory())
                }
            }
        }
    }

    class SyncGroupFactory : LazyNodeFactory<SyncGroup, SyncGroupFactory.InnerPanelContainer> {
        class InnerPanel(group: SyncGroup) : Fragment() {
            private val current = ChangeableValue(group);

            fun update(source: SyncGroup) {
                current.value(source)
            }

            override val root = vbox {
                label(current.map { it.id.toString() }.asBinding())
                this += SyncCommandsFactory.render(current.map { it.commands })
            }
        }

        class InnerPanelContainer(group: SyncGroup) : LazyNodeContainer<SyncGroup> {

            private val panel = InnerPanel(group);

            override fun nodes(): List<Node> {
                return listOf(panel.root);
            }

            fun update(source: SyncGroup) {
                panel.update(source)
            }
        }

        override fun create(index: Int, source: SyncGroup, old: InnerPanelContainer?): InnerPanelContainer {
            return old?.apply {
                this.update(source)
            } ?: InnerPanelContainer(source)
        }
    }
}