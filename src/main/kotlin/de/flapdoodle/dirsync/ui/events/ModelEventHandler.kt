package de.flapdoodle.dirsync.ui.events

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.events.IOEvent
import tornadofx.EventContext

class ModelEventHandler(
        val currentConfig: ChangeableValue<SyncConfig>
) {
    fun instance(): EventContext.(ModelEvent) -> Unit = { event ->
        when (event.data) {
            is ModelEvent.EventData.AddConfig -> {
                currentConfig.value(event.data.applyTo(currentConfig.value()))
            }
            is ModelEvent.EventData.DeleteConfig -> {
                currentConfig.value(event.data.applyTo(currentConfig.value()))
            }
        }
    }
}