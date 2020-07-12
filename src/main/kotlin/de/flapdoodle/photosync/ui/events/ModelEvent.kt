package de.flapdoodle.photosync.ui.events

import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import tornadofx.FXEvent

data class ModelEvent(
        val data: EventData
) : FXEvent() {

    companion object {
        fun addConfig(node: SyncEntry): ModelEvent {
            return EventData.AddConfig(node).asEvent()
        }
    }

    sealed class EventData {
        fun asEvent(): ModelEvent {
            return ModelEvent(this)
        }

        abstract fun applyTo(model: SyncConfig): SyncConfig

        data class AddConfig(
                val config: SyncEntry
        ) : EventData() {
            override fun applyTo(model: SyncConfig): SyncConfig {
                return model.copy(entries = model.entries + config)
            }
        }
    }
}

