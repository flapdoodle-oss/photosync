package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.LazyNodeContainer
import de.flapdoodle.fx.extensions.LazyNodeFactory
import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.extensions.minWithFromPrefered
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
import javafx.scene.control.OverrunStyle
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class SyncCommandsFactory : LazyNodeFactory<SyncGroup.SyncEntry, SyncCommandsFactory.Container> {


    class Container(row: Int, entry: SyncGroup.SyncEntry) : LazyNodeContainer<SyncGroup.SyncEntry> {
        private val current = ChangeableValue(entry);

        private val commandType = Label().apply {
            textProperty().bind(current.map { it.command.javaClass.simpleName ?: "?" }.asBinding())
        }.withPosition(NAME_COLUMN, row)
                .minWithFromPrefered()

        private val src = StretchedLabel(current.map { sourceOf(it.command) }.asBinding(), textOverrunStyle = OverrunStyle.LEADING_ELLIPSIS)
                .root.withPosition(SRC_COLUMN, row)

        private val dst = StretchedLabel(current.map { destinationOf(it.command) }.asBinding(), textOverrunStyle = OverrunStyle.LEADING_ELLIPSIS)
                .root.withPosition(DST_COLUMN, row)

        private val hint = Label().apply { textProperty().bind(current.map { hintOf(it.command) }.asBinding()) }
                .withPosition(HINT_COLUMN, row)
                .minWithFromPrefered()

        private val status = Label().apply {
            textProperty().bind(current.map { it.status.name }.asBinding())
            bindClass(current.map { when(it.status) {
                SyncGroup.Status.Failed -> LabelStyles.failed
                SyncGroup.Status.Successful -> LabelStyles.success
                else -> LabelStyles.unknown
            } }.asBinding())
        }
                .withPosition(STATUS_COLUMN, row)
                .minWithFromPrefered()

        private fun sourceOf(command: SyncCommand): String {
            return when (command) {
                is SyncCommand.Move -> command.src.toString()
                is SyncCommand.Remove -> ""
                is SyncCommand.Copy -> command.src.toString()
                is SyncCommand.CopyBack -> command.src.toString()
            }
        }

        private fun destinationOf(command: SyncCommand): String {
            return when (command) {
                is SyncCommand.Move -> command.dst.toString()
                is SyncCommand.Remove -> command.dst.toString()
                is SyncCommand.Copy -> command.dst.toString()
                is SyncCommand.CopyBack -> command.dst.toString()
            }
        }

        private fun hintOf(command: SyncCommand): String {
            return when (command) {
                is SyncCommand.Move -> ""
                is SyncCommand.Remove -> command.cause.toString()
                is SyncCommand.Copy -> if (command.sameContent) "Same Content" else ""
                is SyncCommand.CopyBack -> if (command.sameContent) "Same Content" else ""
            }
        }

        override fun nodes(): List<Node> {
            return listOf(commandType, src, dst, hint, status)
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
        const val STATUS_COLUMN = 4

        fun render(src: LazyValue<List<SyncGroup.SyncEntry>>): WeightGridPane {
            return WeightGridPane().apply {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                style {
                    padding = box(4.0.px)
//                    borderWidth += box(1.0.px)
//                    borderColor += box(Color.BLUE)
                }
                setColumnWeight(NAME_COLUMN, 0.0)
                setColumnWeight(SRC_COLUMN, 10.0)
                setColumnWeight(DST_COLUMN, 10.0)
                setColumnWeight(HINT_COLUMN, 0.0)
                setColumnWeight(STATUS_COLUMN, 0.0)

                children.bindFromFactory(src, SyncGroup.SyncEntry::command, SyncCommandsFactory())
            }
        }
    }
}