package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.progress.Progress
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncGroupID
import de.flapdoodle.photosync.ui.sync.SyncList

interface Synchronizer {
    fun sync(
            set: SyncList,
            enableCopyBack: Boolean = false,
            enableRemove: Boolean = false,
            listener: (id: SyncGroupID, command: SyncCommand, status: SyncGroup.Status) -> Unit = { _,_,_ -> },
            progressListener: (Progress) -> Unit = { _ -> }
    )
}