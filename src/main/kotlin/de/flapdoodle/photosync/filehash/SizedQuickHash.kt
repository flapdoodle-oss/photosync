package de.flapdoodle.photosync.filehash

import java.nio.file.Path

data class SizedQuickHash(
    private val startHash: String,
    private val size: Long,
    private val endHash: String
) : Hash<SizedQuickHash> {

  companion object : Hasher<SizedQuickHash> {
    private const val BLOCK_SIZE: Int = 512

    override fun toString(): String {
      return SizedQuickHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long): SizedQuickHash {
      return try {
        val firstHash = if (size > 0)
          Hashing.sha256(Hashing.read(path, 0, BLOCK_SIZE))
        else
          ""

        val secondHash = if (size > BLOCK_SIZE)
          Hashing.sha256(Hashing.read(path, size - BLOCK_SIZE, BLOCK_SIZE))
        else
          ""

        SizedQuickHash(firstHash, size, secondHash)
      } catch (ex: Exception) {
        throw RuntimeException("could not hash $path", ex)
      }
    }
  }
}