package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.photosync.Scanner
import de.flapdoodle.photosync.sync.SyncCommandGroup
import java.nio.file.Path
import java.time.LocalDateTime

data class SyncList(
        val srcPath: Path,
        val dstPath: Path,
        val groups: List<SyncGroup>,
        val srcDiskSpaceUsed: Long,
        val dstDiskSpaceUsed: Long,
        val start: LocalDateTime,
        val end: LocalDateTime
) {

    fun isEmpty() = groups.all { it.isEmpty() }

    companion object {
        fun map(
                srcPath: Path,
                dstPath: Path,
                result: Scanner.Result<List<SyncCommandGroup>>
        ): SyncList {
            return SyncList(
                    srcPath,
                    dstPath,
                    result.result.map { SyncGroup(commands = it.commands.map { c -> SyncGroup.SyncEntry(c) }) },
                    result.srcDiskSpaceUsed,
                    result.dstDiskSpaceUsed,
                    result.start,
                    result.end
            )
        }
    }
}