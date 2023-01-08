package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.io.FileIO
import de.flapdoodle.photosync.io.Humans
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Path

data class FullHash(
    val hash: String
) : Hash<FullHash> {

  companion object : Hasher<FullHash> {
    private val HASHED = Statistic.property("Hash.${toString()}", Long::class.java, Long::plus) { "$it" }
    private val HASHED_SIZE = Statistic.property("Hash.${toString()}.size", Long::class.java, Long::plus) { Humans.humanReadableByteCount(it) }

    override fun toString(): String {
      return FullHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): FullHash {
      Statistic.increment(HASHED)
      Statistic.set(HASHED_SIZE, size)
      
      return FullHash(Hashing.sha256 {
        FileIO.readAllBytes(path) { byteBuffer ->
          update(byteBuffer)
        }
      })
    }
  }
}