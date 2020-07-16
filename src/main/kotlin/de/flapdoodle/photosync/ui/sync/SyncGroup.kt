package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.photosync.sync.SyncCommand
import java.util.*

data class SyncGroup(
        val id: UUID = UUID.randomUUID(),
        val commands: List<SyncEntry>
) {

    enum class Status {
        NotExcuted,
        Successful,
        Failed
    }
    data class SyncEntry(val command: SyncCommand, val status: Status = Status.NotExcuted)
}