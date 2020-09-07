package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.LazyNodeContainer
import de.flapdoodle.fx.extensions.LazyNodeFactory
import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.extensions.minWithFromPrefered
import de.flapdoodle.fx.layout.weightflex.Range
import de.flapdoodle.fx.layout.weightflex.WeightFlexPane
import de.flapdoodle.fx.layout.weightflex.updateRow
import de.flapdoodle.fx.layout.weightflex.withPosition
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import de.flapdoodle.photosync.sync.SyncCommand
import de.flapdoodle.photosync.ui.components.StretchedLabel
import de.flapdoodle.types.Grouped
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.layout.Priority
import tornadofx.*

class SyncGroupCommandsFactory : LazyNodeFactory<Grouped<SyncGroup, SyncGroup.SyncEntry>, SyncGroupCommandsFactory.Container>{

    override fun create(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>, old: Container?): Container {
        return old?.apply {
            this.update(index, source)
        } ?: Container(index, source)
    }

    class Container(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>) : LazyNodeContainer<Grouped<SyncGroup, SyncGroup.SyncEntry>> {
        var wrapper = ContainerWrapper.create(index, source)

        override fun nodes(): List<Node> {
            return wrapper.nodes()
        }

        fun update(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>) {
            wrapper = wrapper.update(index, source)
        }
    }

    sealed class ContainerWrapper {
        abstract fun nodes(): List<Node>
        abstract fun update(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>): ContainerWrapper

        class GroupContainer(index: Int, group: SyncGroup): ContainerWrapper() {
            private val current = ChangeableValue(group);

            private val src = StretchedLabel(current.map { it.id.toString() }.asBinding(), textOverrunStyle = OverrunStyle.LEADING_ELLIPSIS)
                    .root.withPosition(Range.of(FIRST_COLUMN, LAST_COLUMN), Range.of(index))

            override fun nodes(): List<Node> {
                return listOf(src)
            }

            override fun update(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>): ContainerWrapper {
                return when (source) {
                    is Grouped.Parent -> {
                        nodes().forEach { it.updateRow(Range.of(index)) }
                        this
                    }
                    else -> create(index,source)
                }
            }
        }

        class EntryContainer(row: Int, entry: SyncGroup.SyncEntry): ContainerWrapper() {
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
                    .withPosition(SyncCommandsFactory.STATUS_COLUMN, row)
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
                return listOf(status, commandType, hint, src, dst)
            }
            
            override fun update(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>): ContainerWrapper {
                return when (source) {
                    is Grouped.Parent -> {
                        nodes().forEach { it.updateRow(Range.of(index)) }
                        this
                    }
                    else -> create(index,source)
                }
            }
        }

        companion object {
            fun create(index: Int, source: Grouped<SyncGroup, SyncGroup.SyncEntry>): ContainerWrapper {
                return when(source) {
                    is Grouped.Parent -> GroupContainer(index,source.parent)
                    is Grouped.Child -> EntryContainer(index,source.child)
                }
            }
        }
    }

    companion object {
        const val STATUS_COLUMN = 0
        const val NAME_COLUMN = 1
        const val HINT_COLUMN = 2
        const val SRC_COLUMN = 3
        const val DST_COLUMN = 4

        const val FIRST_COLUMN = STATUS_COLUMN
        const val LAST_COLUMN = DST_COLUMN

        fun render(groups: LazyValue<List<SyncGroup>>): WeightFlexPane {
            val groupAndEntries = groups.map { Grouped.flatMapGrouped(it) { g -> g to g.commands } }

            return WeightFlexPane().apply {
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

                children.bindFromFactory(groupAndEntries, ::key, SyncGroupCommandsFactory())
            }
        }

        private fun key(src: Grouped<SyncGroup,SyncGroup.SyncEntry>): Any {
            return src.map(parent = {it.id}, child = {p,c-> p.id to c.command})
        }
    }


}