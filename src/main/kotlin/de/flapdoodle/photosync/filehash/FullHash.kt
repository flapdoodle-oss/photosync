package de.flapdoodle.photosync.filehash

import java.nio.file.Files
import java.nio.file.Path

data class FullHash(
    private val hash: String
) {

  companion object {
    fun hash(path: Path) = FullHash(Hashing.sha256(Files.readAllBytes(path)))
  }
}