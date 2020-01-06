package de.flapdoodle.photosync.filehash

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

data class QuickHash(
    private val startHash: String,
    private val endHash: String
) {

  companion object {
    private const val BLOCK_SIZE: Int = 512

    fun hash(path: Path, size: Long): QuickHash {
      val firstBlock = read(path, 0, BLOCK_SIZE)
      val secondBlock = read(path, size - BLOCK_SIZE, BLOCK_SIZE)
      return QuickHash(Hashing.sha256(firstBlock), Hashing.sha256(secondBlock))
    }

    private fun read(path: Path, offset: Long, len: Int): ByteArray {
      return Files.newByteChannel(path).use {
        val buffer = ByteBuffer.allocate(len)
        it.position(offset)
        val readSize = it.read(buffer)
        val block = ByteArray(readSize)
        buffer.get(block, 0, readSize)
        block
      }
    }
  }
}