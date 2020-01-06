package de.flapdoodle.photosync.filehash

import java.nio.file.Files
import java.nio.file.Path

data class FullHash(
    private val hash: String
) : Hash<FullHash> {

  companion object : Hasher<FullHash> {
    override fun hash(path: Path, size: Long) = FullHash(Hashing.sha256(Files.readAllBytes(path)))
  }
}