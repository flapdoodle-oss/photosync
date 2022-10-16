package de.flapdoodle.io.filetree.diff.withmeta

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ParsedDarktableFileNameTest {

  @Test
  fun imageFileName() {
    assertThat(ParsedDarktableFileName.parse("foo123.bar"))
      .isEqualTo(
        ParsedDarktableFileName(
          "foo123", null, listOf("bar")
        )
      )
  }

  @Test
  fun imageFileNameWithVersion() {
    assertThat(ParsedDarktableFileName.parse("foo123_012.bar"))
      .isEqualTo(
        ParsedDarktableFileName(
          "foo123", "_012", listOf("bar")
        )
      )
  }

  @Test
  fun metaFile() {
    assertThat(ParsedDarktableFileName.parse("foo123.bar.meta"))
      .isEqualTo(
        ParsedDarktableFileName(
          "foo123", null, listOf("bar", "meta")
        )
      )
  }
}