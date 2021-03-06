package de.flapdoodle.dirsync.ui.events

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.dirsync.ui.config.SyncEntry
import tornadofx.FXEvent
import java.util.*

data class ModelEvent(
        val data: EventData
) : FXEvent() {

    companion object {
        fun addOrChangeConfig(entry: SyncEntry): ModelEvent {
            return EventData.AddOrChangeConfig(entry).asEvent()
        }

        fun deleteConfig(id: UUID): ModelEvent {
            return EventData.DeleteConfig(id).asEvent()
        }
    }

    sealed class EventData {
        fun asEvent(): ModelEvent {
            return ModelEvent(this)
        }

        abstract fun applyTo(model: SyncConfig): SyncConfig

        data class AddOrChangeConfig(
                val config: SyncEntry
        ) : EventData() {
            override fun applyTo(model: SyncConfig): SyncConfig {
                return model.copy(entries = model.entries.filter { it.id == config.id } + config)
            }
        }

        data class DeleteConfig(
                val id: UUID
        ) : EventData() {
            override fun applyTo(model: SyncConfig): SyncConfig {
                require(model.entries.any { it.id == id }) { "could not find $id in $model"}
                return model.copy(entries = model.entries.filter { it.id != id })
            }
        }
    }
}

