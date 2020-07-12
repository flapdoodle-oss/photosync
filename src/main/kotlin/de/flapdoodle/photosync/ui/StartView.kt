package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ModelEvent
import tornadofx.*

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
}