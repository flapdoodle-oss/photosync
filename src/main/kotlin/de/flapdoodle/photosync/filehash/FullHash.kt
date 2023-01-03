package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.io.FileIO
import java.nio.file.Files
import java.nio.file.Path

data class FullHash(
    private val hash: String
) : Hash<FullHash> {

  companion object : Hasher<FullHash> {
    override fun toString(): String {
      return FullHash::class.java.simpleName
    }

    override fun hash(path: Path, size: Long): FullHash {
      return FullHash(Hashing.sha256 {
        FileIO.readAllBytes(path) { byteBuffer ->
          update(byteBuffer)
        }
      })
    }
  }
}