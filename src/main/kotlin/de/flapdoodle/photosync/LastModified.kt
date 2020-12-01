package de.flapdoodle.photosync

import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit

data class LastModified(
    private val value: Instant
) : Comparable<LastModified> {

  override fun compareTo(other: LastModified): Int {
    return value.compareTo(other.value)
  }

  operator fun plus(offsetInSeconds: Int): LastModified {
    return LastModified(value.plus(offsetInSeconds.toLong(), ChronoUnit.SECONDS))
  }

  companion object {
    fun from(fileTime: FileTime): LastModified {
      val instant = fileTime.toInstant()
      return LastModified(instant.truncatedTo(ChronoUnit.SECONDS))
    }

    fun now(): LastModified {
      return LastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS))
    }

    fun asFileTime(lastModified: LastModified): FileTime {
      return FileTime.from(lastModified.value)
    }
  }
}