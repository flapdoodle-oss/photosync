package de.flapdoodle.dirsync.ui.events

import tornadofx.FXEvent
import java.util.*

data class ActionEvent(val action: Action) : FXEvent() {
    companion object {
        fun startScan(id: UUID) = ActionEvent(Action.StartScan(id))
        fun stopScan(id: UUID) = ActionEvent(Action.StopScan(id))
        fun scanFinished(id: UUID) = ActionEvent(Action.ScanFinished(id))
    }

    sealed class Action {
        data class StartScan(val id: UUID) : Action()
        data class StopScan(val id: UUID) : Action()
        data class ScanFinished(val id: UUID) : Action()
    }
}