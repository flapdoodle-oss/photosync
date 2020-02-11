package de.flapdoodle.photosync.paths

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class MetaTest {
  @Test
  fun `base path of path`() {
    val meta = Path.of("foo", "bar.txt")
    val base = Path.of("foo", "bar")

    assertThat(Meta.isMeta(meta, base)).isTrue()
    assertThat(Meta.isMeta(base, base)).isFalse()
    assertThat(Meta.isMeta(base, meta)).isFalse()
  }

  @Test
  fun `meta can add version before file extension`() {
    val meta = Path.of("foo", "bar_01.CR2.xmp")
    val base = Path.of("foo", "bar.CR2")

    assertThat(Meta.isMeta(meta, base)).isTrue()
    assertThat(Meta.isMeta(base, base)).isFalse()
    assertThat(Meta.isMeta(base, meta)).isFalse()
  }

  @Test
  fun `replace base`() {
    val meta = Path.of("foo", "bar.txt")
    val base = Path.of("foo", "bar")
    val dest = Path.of("bar", "blob")

    val result = Meta.replaceBase(meta, base, dest)

    assertThat(result).isEqualTo(Path.of("bar", "blob.txt"))
  }

  @Test
  fun `replace base if counter is present`() {
    val meta = Path.of("foo", "bar_01.txt")
    val base = Path.of("foo", "bar")
    val dest = Path.of("bar", "blob")

    val result = Meta.replaceBase(meta, base, dest)

    assertThat(result).isEqualTo(Path.of("bar", "blob_01.txt"))
  }
}