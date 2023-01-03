package de.flapdoodle.photosync.io

import de.flapdoodle.photosync.progress.Statistic
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.text.CharacterIterator
import java.text.StringCharacterIterator


object FileIO {
  private val BYTES_READ=Statistic.property("bytes read", Long::class.java, Long::plus) {
    humanReadableByteCount(it)
  }

  fun readAllBytes(path: Path, blocksize: Int = 512, onBlock: (ByteBuffer) -> Unit) {
    Files.newByteChannel(path).use { channel ->
      val buffer: ByteBuffer = ByteBuffer.allocate(blocksize)
      while (channel.read(buffer) > 0) {
        buffer.flip()
        Statistic.set(BYTES_READ, buffer.limit().toLong())
        onBlock(buffer.asReadOnlyBuffer())
        buffer.clear()
      }
    }
  }

  fun read(path: Path, offset: Long, len: Int, onBlock: (ByteBuffer, Int) -> Unit) {
    Files.newByteChannel(path).use { channel ->
      val buffer = ByteBuffer.allocate(len)

      channel.position(offset)
      val readSize = channel.read(buffer)
      buffer.flip()
      Statistic.set(BYTES_READ, buffer.limit().toLong())

      onBlock(buffer.asReadOnlyBuffer(), readSize)
//      val block = ByteArray(readSize)
//      buffer.get(block, 0, readSize)
//      block
    }
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