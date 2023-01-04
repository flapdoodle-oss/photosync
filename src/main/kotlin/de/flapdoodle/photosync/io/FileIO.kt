package de.flapdoodle.photosync.io

import de.flapdoodle.photosync.progress.Statistic
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
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
    return read(path) {
      read(offset, len)
    }
  }

  fun <T> read(path: Path, action: ChannelAdapter.() -> T): T {
    Files.newByteChannel(path).use { channel ->
      return action(ChannelWrapper(channel))
    }
  }

  class BufferCache {
    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
    private var byteArray: ByteArray = ByteArray(0)

    fun <T> withByteBuffer(size: Int, action: BufferCache.(ByteBuffer) -> T): T {
      val buffer = if (byteBuffer.remaining()==size) {
        byteBuffer
      } else {
        byteBuffer = ByteBuffer.allocate(size)
        byteBuffer
      }

      try {
        return action(buffer)
      } finally {
        buffer.clear()
      }
    }

    fun <T> withByteArray(size: Int, action: (ByteArray) -> T): T {
      val array = if (byteArray.size == size) {
        byteArray
      } else {
        byteArray = ByteArray(size)
        byteArray
      }
      return action(array)
    }

  }

  interface ChannelAdapter {
    fun <T> read(offset: Long, len: Int, action: (ByteArray) -> T): T

    fun read(offset: Long, len: Int): ByteArray {
      return read(offset, len) {
        it.copyOf()
      }
    }
  }

  class ChannelWrapper(val channel: SeekableByteChannel):ChannelAdapter {
    private val bufferCache = BufferCache()

    override fun <T> read(offset: Long, len: Int, action: (ByteArray) -> T): T {
      return bufferCache.withByteBuffer(len) { buffer ->

        channel.position(offset)
        val readSize = channel.read(buffer)
        buffer.flip()
        Statistic.set(BYTES_READ, buffer.limit().toLong())

        withByteArray(readSize) { byteArray ->
          buffer.get(byteArray, 0, readSize)
          action(byteArray)
        }
      }
    }
  }
}