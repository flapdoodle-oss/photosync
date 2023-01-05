package de.flapdoodle.photosync

import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

data class MockedHasher(
    private val rules: List<(Path, Long) -> MockHash?> = emptyList()
) : Hasher<MockedHasher.MockHash> {

  private fun addRule(rule: (Path, Long) -> MockHash?): MockedHasher {
    return this.copy(rules + rule)
  }

  fun addRule(path: Path, size: Long, hash: String): MockedHasher {
    return addRule(rule = { p, s ->
      if (p == path && s == size) MockHash(hash)
      else null
    })
  }

  override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): MockHash {
    rules.forEach {
      val result = it(path, size)
      if (result != null) {
        return result
      }
    }
    throw IllegalArgumentException("no rule found for $path:$size")
  }

  data class MockHash(
      private val key: String
  ) : Hash<MockHash>
}