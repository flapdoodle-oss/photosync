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
import java.util.*

class SyncModalView : View("Sync") {
    private val result = ChangeableValue<Optional<SyncList>>(Optional.empty())

    private val timeUsedInSeconds = result.map { it.map { "Scanned in ${Duration.between(it.start, it.end).toSeconds()}s" }.orElse("") }

    private val syncCommandGroups = result.map { it.map { it.groups.filter { it.commands.isNotEmpty() } }.orElse(emptyList()) }

    override val root = borderpane {
        top {
            hbox {
                label(timeUsedInSeconds.asBinding())
                label(result.map {
                    it.map { "Diskspace used: ${it.srcDiskSpaceUsed / (1024 * 1024)} MB - ${it.dstDiskSpaceUsed / (1024 * 1024)} MB" }.orElse("")
                }.asBinding())

                button("execute") {
                    subscribeEvent<ActionEvent> {event ->
                        if (event.action is ActionEvent.Action.SyncDone) {
                            isDisable = false
                        }
                        if (event.action is ActionEvent.Action.Synced) {
                            result.value { it.map { update(it, event.action) } }
                        }
                    }
                    action {
                        isDisable = true
                        result.value().ifPresent {
                            ActionEvent.sync(it).fire()
                        }
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
            view.result.value(Optional.of(result))
            view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
        }
    }

}