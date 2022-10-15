package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.photosync.sync.SyncCommand

data class SyncGroup(
        val id: SyncGroupID = SyncGroupID(),
        val commands: List<SyncEntry>
) {
    fun isEmpty() = commands.isEmpty()

    enum class Status {
        NotExcuted,
        Successful,
        Failed
    }
    data class SyncEntry(val command: SyncCommand, val status: Status = Status.NotExcuted)
}