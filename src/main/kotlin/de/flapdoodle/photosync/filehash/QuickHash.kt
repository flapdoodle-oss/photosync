package de.flapdoodle.photosync.filehash

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

data class QuickHash(
    private val startHash: String,
    private val endHash: String
) : Hash<QuickHash> {

  companion object : Hasher<QuickHash> {
    private const val BLOCK_SIZE: Int = 512

    override fun hash(path: Path, size: Long): QuickHash {
      return try {
        if (size>0) {
          val firstBlock = read(path, 0, BLOCK_SIZE)
          val secondBlock = read(path, size - BLOCK_SIZE, BLOCK_SIZE)
          QuickHash(Hashing.sha256(firstBlock), Hashing.sha256(secondBlock))
        } else {
          QuickHash("","")
        }
      } catch (ex: Exception) {
        throw RuntimeException("could not hash $path", ex)
      }
    }

    private fun read(path: Path, offset: Long, len: Int): ByteArray {
      return Files.newByteChannel(path).use {
        val buffer = ByteBuffer.allocate(len)
        it.position(offset)
        val readSize = it.read(buffer)
        buffer.flip()

        val block = ByteArray(readSize)
        buffer.get(block, 0, readSize)
        block
      }
    }
  }
}