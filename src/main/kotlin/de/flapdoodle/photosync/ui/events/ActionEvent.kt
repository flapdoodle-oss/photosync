package de.flapdoodle.photosync.ui.events

import tornadofx.FXEvent
import java.util.*

data class ActionEvent(val action: Action) : FXEvent() {

    companion object {
        fun startSync(id: UUID) = ActionEvent(Action.StartSync(id))
        fun syncFinished(id: UUID) = ActionEvent(Action.SyncFinished(id))
    }

    sealed class Action {
        data class StartSync(val id: UUID) : Action()

        data class SyncFinished(val id: UUID) : Action()
    }
}