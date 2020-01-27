package de.flapdoodle.photosync

import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

operator fun FileTime.plus(offsetInSeconds: Int): FileTime {
  return FileTime.from(this.toInstant().plus(offsetInSeconds.toLong(), ChronoUnit.SECONDS))
}

object FileTimes{
  fun now(): FileTime = FileTime.from(Instant.now())
}

