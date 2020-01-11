package de.flapdoodle.photosync

import java.nio.file.attribute.FileTime
import java.time.Instant

object FileTimes{
  fun now(): FileTime = FileTime.from(Instant.now())
}