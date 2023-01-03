package de.flapdoodle.photosync.filehash

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class HashingTest {

  @Test
  fun sampleHash() {
    val hash = Hashing.sha256(ByteArray(512))

    assertThat(hash).isEqualTo("076a27c79e5ace2a3d47f9dd2e83e4ff6ea8872b3c2218f66c92b89b55f36560")
  }

  @Test
  fun sampleHashWithCallback() {
    val hash = Hashing.sha256 {
      update(ByteArray(512))
    }

    assertThat(hash).isEqualTo("076a27c79e5ace2a3d47f9dd2e83e4ff6ea8872b3c2218f66c92b89b55f36560")
  }
}