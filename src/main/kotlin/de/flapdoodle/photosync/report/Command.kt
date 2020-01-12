package de.flapdoodle.photosync.report

import java.nio.file.Path

abstract sealed class Command {
  data class Move(val src: Path, val dst: Path) : Command()
  data class Copy(val src: Path, val dst: Path) : Command()
  data class Rm(val path: Path): Command()
}