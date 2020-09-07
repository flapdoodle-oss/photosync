package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.*
import de.flapdoodle.photosync.ui.AddSyncConfigView
import de.flapdoodle.photosync.ui.events.ActionEvent
import tornadofx.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class SyncModalView : View("Sync") {
    private val model = SyncConfigModel(MutableSyncConfig())

    private val result = ChangeableValue<Optional<SyncList>>(Optional.empty())
    private val resultIsEmpty: LazyValue<Boolean> = result.map { it.map { it.isEmpty() }.orElse(true) }
    private val syncRunning: ChangeableValue<Boolean> = ChangeableValue(false)
    private val buttonIsDisabled = resultIsEmpty.merge(syncRunning, map = { a: Boolean, b: Boolean -> a || b })

    private val timeUsedInSeconds = result.map { it.map { "Scanned in ${Duration.between(it.start, it.end).toSeconds()}s" }.orElse("") }

    private val syncCommandGroups: LazyValue<List<SyncGroup>> = result.map { it.map { it.groups.filter { it.commands.isNotEmpty() } }.orElse(emptyList()) }

    override val root = borderpane {
        top {
            hbox {
                label(timeUsedInSeconds.asBinding())
                label(result.map {
                    it.map { "Diskspace used: ${it.srcDiskSpaceUsed / (1024 * 1024)} MB - ${it.dstDiskSpaceUsed / (1024 * 1024)} MB" }.orElse("")
                }.asBinding())

                button(resultIsEmpty.map { if (it)  "no change" else "execute" }.asBinding()) {
                    subscribeEvent<ActionEvent> {event ->
                        if (event.action is ActionEvent.Action.SyncDone) {
                            syncRunning.value(false)
                        }
                        if (event.action is ActionEvent.Action.Synced) {
                            result.value { it.map { update(it, event.action) } }
                        }
                    }
                    action {
                        result.value().ifPresent {
                            syncRunning.value(true)
                            ActionEvent.sync(it,
                                    enableCopyBack = model.copyBack.value,
                                    enableRemove = model.remove.value
                            ).fire()
                        }
                    }
                    disableProperty().bind(buttonIsDisabled.asBinding())
                }

                checkbox("Copy Back", model.copyBack) {

                }
                checkbox("Remove", model.remove) {

                }
            }
        }
        center {
            scrollpane {
                if (false) {
                    vbox {
                        children.bindFromFactory(syncCommandGroups, SyncGroup::id, SyncGroupFactory())
                    }
                }
                this += SyncGroupCommandsFactory.render(syncCommandGroups)
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

    class MutableSyncConfig(var copyBack: Boolean = false, var remove: Boolean = false)

    class SyncConfigModel(initialValue: MutableSyncConfig) : ItemViewModel<MutableSyncConfig>(initialValue) {
        val copyBack = bind(MutableSyncConfig::copyBack)
        val remove = bind(MutableSyncConfig::remove)
    }

}