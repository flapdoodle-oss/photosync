package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.io.FileIO
import de.flapdoodle.photosync.io.Humans
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Path

data class SizedQuickHash(
    val startHash: String,
    val size: Long,
    val endHash: String
) : Hash<SizedQuickHash> {

  companion object : Hasher<SizedQuickHash> {
    private const val BLOCK_SIZE: Int = 512
    private val HASHED = Statistic.property("Hash.${toString()}", Long::class.java, Long::plus) { "$it" }
    private val HASHED_READ = Statistic.property("Hash.${toString()}.read", Long::class.java, Long::plus) { Humans.humanReadableByteCount(it) }
    private val HASHED_SIZE = Statistic.property("Hash.${toString()}.size", Long::class.java, Long::plus) { Humans.humanReadableByteCount(it) }

    fun blockSize() = BLOCK_SIZE

    override fun toString(): String {
      return SizedQuickHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): SizedQuickHash {
      Statistic.increment(HASHED)
      Statistic.set(HASHED_SIZE, size)

      return try {
        FileIO.read(path) {
          val firstHash = if (size > 0) {
            Statistic.set(HASHED_READ, BLOCK_SIZE.toLong())
            Hashing.sha256(read(0, BLOCK_SIZE))
          } else {
            ""
          }

          val secondHash = if (size > BLOCK_SIZE) {
            Statistic.set(HASHED_READ, BLOCK_SIZE.toLong())
            Hashing.sha256(read( size - BLOCK_SIZE, BLOCK_SIZE))
          } else
            ""

          SizedQuickHash(firstHash, size, secondHash)
        }
//
//        val firstHash = if (size > 0) {
//          Statistic.set(HASHED_READ, BLOCK_SIZE.toLong())
//          Hashing.sha256(FileIO.read(path, 0, BLOCK_SIZE))
//        } else
//          ""
//
//        val secondHash = if (size > BLOCK_SIZE) {
//          Statistic.set(HASHED_READ, BLOCK_SIZE.toLong())
//          Hashing.sha256(FileIO.read(path, size - BLOCK_SIZE, BLOCK_SIZE))
//        } else
//          ""
//
//        SizedQuickHash(firstHash, size, secondHash)
      } catch (ex: Exception) {
        throw RuntimeException("could not hash $path", ex)
      }
    }
  }
}