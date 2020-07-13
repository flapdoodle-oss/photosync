package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ActionEvent
import de.flapdoodle.photosync.ui.events.ModelEvent
import tornadofx.*
import java.util.*

class StartView : View("PhotoSync") {

    private val currentConfig = ChangeableValue(SyncConfig(listOf(
            SyncEntry(src = "src", dst="dst"),
            SyncEntry(src = "foo/2020", dst="bar/2020")
    )))

    override val root = borderpane {
        subscribeEvent<ModelEvent> {event ->
            when (event.data) {
                is ModelEvent.EventData.AddConfig -> {
                    currentConfig.value(event.data.applyTo(currentConfig.value()))
                }
                is ModelEvent.EventData.DeleteConfig -> {
                    currentConfig.value(event.data.applyTo(currentConfig.value()))
                }
                else -> {
                    throw IllegalArgumentException("unknown event: ${event}")
                }
            }
        }

        subscribeEvent<ActionEvent> { event ->
            println("Action found: $event")
            
            when(event.action) {
                is ActionEvent.Action.StartSync -> {
                    runAsync {
                        startSync(currentConfig.value(), event.action.id)
                    } ui {
                      ActionEvent.syncFinished(event.action.id).fire()
                    }
                }
            }
        }

        top {
            menubar {
                menu("Files") {
                    item("Open") {
                        action {
                            //IOEvent.load().fire()
                        }
                    }

                    item("Save") {
                        action {
                            //IOEvent.save().fire()
                        }
                    }
                }
            }
        }

        center = SyncConfigView(currentConfig).root
        
    }

    private fun startSync(config: SyncConfig, id: UUID) {
        val matching = config.entries.filter { it.id == id }
        require(matching.size == 1) {"could not find entry $id in $config"}

        println("sync $matching")
    }
}