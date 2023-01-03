package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.io.FileIO
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Files
import java.nio.file.Path

data class FullHash(
    private val hash: String
) : Hash<FullHash> {

  companion object : Hasher<FullHash> {
    private val HASHED = Statistic.property(toString(), Long::class.java, Long::plus) { "$it" }

    override fun toString(): String {
      return FullHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long): FullHash {
      Statistic.set(HASHED, 1)
      
      return FullHash(Hashing.sha256 {
        FileIO.readAllBytes(path) { byteBuffer ->
          update(byteBuffer)
        }
      })
    }
  }
}