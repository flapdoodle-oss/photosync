package de.flapdoodle.photosync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.regex.Pattern

internal class PathsKtTest {

  @Test
  fun `base path of path`() {
    val meta = Path.of("foo", "bar.txt")
    val base = Path.of("foo", "bar")

    assertThat(meta.isMetaOf(base)).isTrue()
    assertThat(base.isMetaOf(base)).isFalse()
    assertThat(base.isMetaOf(meta)).isFalse()
  }

  @Test
  fun `meta can add version before file extension`() {
    val meta = Path.of("foo", "bar_01.CR2.xmp")
    val base = Path.of("foo", "bar.CR2")

    assertThat(meta.isMetaOf(base)).isTrue()
    assertThat(base.isMetaOf(base)).isFalse()
    assertThat(base.isMetaOf(meta)).isFalse()
  }

  @Test
  fun `replace base`() {
    val meta = Path.of("foo", "bar.txt")
    val base = Path.of("foo", "bar")
    val dest = Path.of("bar", "blob")

    val result = meta.replaceBase(base, dest)

    assertThat(result).isEqualTo(Path.of("bar", "blob.txt"))
  }

  @Test
  fun `regex should match path`() {
    val path = Path.of("some", "year", "2019", "files", "file.txt")
    val regex = Pattern.compile("year/20")

    assertThat(path.matches(regex)).isTrue()
  }

  @Test
  fun `regex should not match path`() {
    val path = Path.of("some", "year", "2019", "files", "file.txt")
    val regex = Pattern.compile("year/19")

    assertThat(path.matches(regex)).isFalse()
  }
}