package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.LazyNodeContainer
import de.flapdoodle.fx.extensions.LazyNodeFactory
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import javafx.scene.Node
import tornadofx.Fragment
import tornadofx.label
import tornadofx.plusAssign
import tornadofx.vbox

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