package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.progress.Progress
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncGroupID
import de.flapdoodle.photosync.ui.sync.SyncList

class NIOSynchronizer : Synchronizer {
    override fun sync(
            set: SyncList,
            listener: (id: SyncGroupID, command: SyncCommand, status: SyncGroup.Status) -> Unit,
            progressListener: (Progress) -> Unit
    ) {
        val max = set.groups.size;

        set.groups.forEachIndexed { index, group ->
            group.commands.forEach {
                when (it.status) {
                    SyncGroup.Status.NotExcuted -> {
                        listener(group.id, it.command, SyncGroup.Status.Failed)
                        Thread.sleep(100);
                    }
                }
            }
            progressListener(Progress(index.toLong(), max.toLong()))
        }
    }
}