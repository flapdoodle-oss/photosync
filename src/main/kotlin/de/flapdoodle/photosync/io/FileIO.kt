package de.flapdoodle.photosync.io

import de.flapdoodle.photosync.progress.Statistic
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path


object FileIO {
  private val BYTES_READ=Statistic.property("FileIO.read", Long::class.java, Long::plus) {
    Humans.humanReadableByteCount(it)
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

  fun read(path: Path, offset: Long, len: Int): ByteArray {
    var result: ByteArray? = null
    read(path, offset, len) { byteBuffer, size ->
      result = ByteArray(size)
      byteBuffer.get(result, 0, size)
    }
    return result ?: throw IOException("could not read into buffer")
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
}