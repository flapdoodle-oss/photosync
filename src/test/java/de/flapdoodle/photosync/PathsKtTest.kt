package de.flapdoodle.photosync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class PathsKtTest {

  @Test
  fun `base path of path`() {
    val meta = Path.of("foo","bar.txt")
    val base = Path.of("foo","bar")

    assertThat(meta.isMetaOf(base)).isTrue()
    assertThat(base.isMetaOf(base)).isFalse()
    assertThat(base.isMetaOf(meta)).isFalse()
  }
}