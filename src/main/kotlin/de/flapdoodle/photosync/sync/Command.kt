package de.flapdoodle.photosync.sync

import java.nio.file.Path

sealed class Command() {
  enum class Cause {
    DeletedEntry,
    CopyRemovedFromSource
  }
  data class Move(val dst: Path, val newDst: Path) : Command()
  data class Copy(val src: Path, val dst: Path) : Command()
  data class Remove(val dst: Path, val cause: Cause): Command()
}