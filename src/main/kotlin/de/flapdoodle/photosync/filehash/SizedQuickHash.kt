package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.io.FileIO
import de.flapdoodle.photosync.io.Humans
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Path

data class SizedQuickHash(
    private val startHash: String,
    private val size: Long,
    private val endHash: String
) : Hash<SizedQuickHash> {

  companion object : Hasher<SizedQuickHash> {
    private const val BLOCK_SIZE: Int = 512
    private val HASHED = Statistic.property("Hash.${toString()}", Long::class.java, Long::plus) { "$it" }
    private val HASHED_SIZE = Statistic.property("Hash.${toString()}.size", Long::class.java, Long::plus) { Humans.humanReadableByteCount(it) }

    override fun toString(): String {
      return SizedQuickHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long): SizedQuickHash {
      Statistic.increment(HASHED)
      Statistic.set(HASHED_SIZE, size)

      return try {
        val firstHash = if (size > 0)
          Hashing.sha256(FileIO.read(path, 0, BLOCK_SIZE))
        else
          ""

        val secondHash = if (size > BLOCK_SIZE)
          Hashing.sha256(FileIO.read(path, size - BLOCK_SIZE, BLOCK_SIZE))
        else
          ""

        SizedQuickHash(firstHash, size, secondHash)
      } catch (ex: Exception) {
        throw RuntimeException("could not hash $path", ex)
      }
    }
  }
}