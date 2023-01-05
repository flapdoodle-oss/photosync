package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

@Deprecated(message = "use SizedQuickHash")
data class QuickHash(
    private val startHash: String,
    private val endHash: String
) : Hash<QuickHash> {

  companion object : Hasher<QuickHash> {
    private const val BLOCK_SIZE: Int = 512

    override fun toString(): String {
      return QuickHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): QuickHash {
      return try {
        val secondHash = if (size > BLOCK_SIZE)
          Hashing.sha256(Hashing.read(path, size - BLOCK_SIZE, BLOCK_SIZE))
        else
          ""

        val firstHash = if (size > 0)
          Hashing.sha256(Hashing.read(path, 0, BLOCK_SIZE))
        else
          ""

        QuickHash(firstHash, secondHash)
      } catch (ex: Exception) {
        throw RuntimeException("could not hash $path", ex)
      }
    }
  }
}