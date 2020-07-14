package de.flapdoodle.photosync.ui.events

import tornadofx.FXEvent
import java.util.*

data class ActionEvent(val action: Action) : FXEvent() {

    companion object {
        fun startSync(id: UUID) = ActionEvent(Action.StartSync(id))
        fun stopSync(id: UUID) = ActionEvent(Action.StopSync(id))
        fun syncStarted(id: UUID) = ActionEvent(Action.SyncStarted(id))
        fun syncAborted(id: UUID) = ActionEvent(Action.SyncAborted(id))
        fun syncFinished(id: UUID) = ActionEvent(Action.SyncFinished(id))
    }

    sealed class Action {
        data class StartSync(val id: UUID) : Action()
        data class StopSync(val id: UUID) : Action()

        data class SyncStarted(val id: UUID) : Action()
        data class SyncAborted(val id: UUID) : Action()
        data class SyncFinished(val id: UUID) : Action()
    }
}