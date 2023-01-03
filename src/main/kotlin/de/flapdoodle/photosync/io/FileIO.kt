package de.flapdoodle.photosync.io

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path


object FileIO {
  fun readAllBytes(path: Path, blocksize: Int = 512, onBlock: (ByteBuffer) -> Unit) {
    Files.newByteChannel(path).use { channel ->
      val buffer: ByteBuffer = ByteBuffer.allocate(blocksize)
      while (channel.read(buffer) > 0) {
        buffer.flip()
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

      onBlock(buffer.asReadOnlyBuffer(), readSize)
//      val block = ByteArray(readSize)
//      buffer.get(block, 0, readSize)
//      block
    }
  }

}