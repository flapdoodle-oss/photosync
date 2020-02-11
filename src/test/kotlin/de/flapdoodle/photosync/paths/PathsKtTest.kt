package de.flapdoodle.photosync.paths

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.regex.Pattern

internal class PathsKtTest {


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