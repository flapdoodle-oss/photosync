package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.filehash.SizedQuickHash
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PersistSizedQuickHashTest {
  @Test
  fun canPersistHash() {
    val hash = SizedQuickHash("foo",123L,"bar")

    val asString = PersistSizedQuickHash.toString(hash)

    assertThat(asString).isEqualTo("512:foo:123:bar")

    val readBack = PersistSizedQuickHash.fromString(asString)

    assertThat(readBack).isEqualTo(hash)
  }
}