package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.analyze.GroupSameContent
import java.nio.file.Path

sealed class Command {
  enum class Cause {
    DeletedEntry,
    CopyRemovedFromSource
  }
  // only in destination
  data class Move(val src: Path, val dst: Path) : Command()
  // only in destination
  data class BulkMove(val src: Path, val dst: Path, val cause: List<SyncCommand.Move>) : Command()
  // only in destination
  data class MkDir(val dst: Path) : Command()

  data class Copy(val src: Path, val dst: Path, val sameContent: Boolean) : Command()
  data class CopyBack(val src: Path, val dst: Path) : Command()
  // only in destination
  data class Remove(val dst: Path, val cause: Cause): Command()
}