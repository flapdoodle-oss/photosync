package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import de.flapdoodle.photosync.ui.events.ActionEvent
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

                button("execute") {
                    subscribeEvent<ActionEvent> {event ->
                        if (event.action is ActionEvent.Action.SyncDone) {
                            isDisable = false
                        }
                        if (event.action is ActionEvent.Action.Synced) {
                            result.value { update(it, event.action) }
                        }
                    }
                    action {
                        isDisable = true
                        ActionEvent.sync(result.value()).fire()
                    }
                }
            }
        }
        center {
            vbox {
                children.bindFromFactory(syncCommandGroups, SyncGroup::id, SyncGroupFactory())
            }
        }
    }

    private fun update(src: SyncList, action: ActionEvent.Action.Synced): SyncList {
        return src.copy(groups = src.groups.map {g ->
            if (g.id==action.id) g.copy(commands = g.commands.map {c ->
                if (c.command==action.command) c.copy(status = action.status)
                else c
            })
            else g
        })
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

}