package de.flapdoodle.photosync.ui.events

import de.flapdoodle.photosync.sync.SyncCommand
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncGroupID
import de.flapdoodle.photosync.ui.sync.SyncList
import tornadofx.FXEvent
import java.util.UUID

data class ActionEvent(val action: Action) : FXEvent() {

    companion object {
        fun startScan(id: UUID) = ActionEvent(Action.StartScan(id))
        fun stopScan(id: UUID) = ActionEvent(Action.StopScan(id))
        fun scanStarted(id: UUID) = ActionEvent(Action.ScanStarted(id))
        fun scanAborted(id: UUID) = ActionEvent(Action.ScanAborted(id))
        fun scanFinished(id: UUID, data: SyncList) = ActionEvent(Action.ScanFinished(id, data))

        fun sync(data: SyncList) = ActionEvent(Action.Sync(data))
        fun synced(id: SyncGroupID, command: SyncCommand, status: SyncGroup.Status) = ActionEvent(Action.Synced(id, command, status))
        fun syncDone() = ActionEvent(Action.SyncDone)
    }

    sealed class Action {
        data class StartScan(val id: UUID) : Action()
        data class StopScan(val id: UUID) : Action()

        data class ScanStarted(val id: UUID) : Action()
        data class ScanAborted(val id: UUID) : Action()
        data class ScanFinished(val id: UUID, val data: SyncList) : Action()

        data class Sync(val data: SyncList): Action()
        data class Synced(val id: SyncGroupID, val command: SyncCommand, val status: SyncGroup.Status): Action()
        object SyncDone: Action()
    }
}