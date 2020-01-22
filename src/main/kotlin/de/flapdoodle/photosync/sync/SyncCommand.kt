package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.expectParent
import java.nio.file.Path

fun List<SyncCommand>.bulkMove():SyncCommand.Move? {
  if (isNotEmpty()) {
    val moveCommands = filterIsInstance<SyncCommand.Move>()
    if (moveCommands.size == size) {
      val dirSet = moveCommands.map { it.src.expectParent() to it.dst.expectParent() }.toSet()
      if (dirSet.size==1) {
        if (moveCommands.all { it.src.fileName == it.dst.fileName }) {
          val directories = dirSet.single()
          return SyncCommand.Move(directories.first, directories.second)
        }
      }
    }
  }
  return null
}

sealed class SyncCommand {
  enum class Cause {
    DeletedEntry,
    CopyRemovedFromSource
  }
  // only in destination
  data class Move(val src: Path, val dst: Path) : SyncCommand()
  data class Copy(val src: Path, val dst: Path) : SyncCommand()
  // only in destination
  data class Remove(val dst: Path, val cause: Cause): SyncCommand()
}