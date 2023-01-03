package de.flapdoodle.photosync.io

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.time.Duration

object Humans {

  fun asHumanReadable(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart().toLong()
    val seconds = duration.toSecondsPart().toLong()
    val millis = duration.toMillisPart().toLong()

    return String.format("%dh %dm %ds %dms", hours, minutes, seconds, millis)
  }

  fun humanReadableByteCount(bytes: Long): String {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
    if (absB < 1024) {
      return "$bytes B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
      value = value shr 10
      ci.next()
      i -= 10
    }
    value *= java.lang.Long.signum(bytes).toLong()
    return String.format("%.1f %ciB", value / 1024.0, ci.current()) + " ($absB B)"
  }

}