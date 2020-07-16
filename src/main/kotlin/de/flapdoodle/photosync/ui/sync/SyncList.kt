package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.photosync.Scanner
import de.flapdoodle.photosync.sync.SyncCommandGroup
import java.time.LocalDateTime

data class SyncList(
        val groups: List<SyncGroup>,
        val srcDiskSpaceUsed: Long,
        val dstDiskSpaceUsed: Long,
        val start: LocalDateTime,
        val end: LocalDateTime
) {

    companion object {
        fun map(src: Scanner.Result<List<SyncCommandGroup>>): SyncList {
            return SyncList(
                    src.result.map { SyncGroup(commands = it.commands.map { c -> SyncGroup.SyncEntry(c) }) },
                    src.srcDiskSpaceUsed,
                    src.dstDiskSpaceUsed,
                    src.start,
                    src.end
            )
        }
    }
}