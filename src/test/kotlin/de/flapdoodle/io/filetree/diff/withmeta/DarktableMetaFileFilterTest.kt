package de.flapdoodle.io.filetree.diff.withmeta

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test

internal class DarktableMetaFileFilterTest {

  val testee = DarktableMetaFileFilter()

  @Test
  fun shortNameIsBaseFileForLongName() {
    val metaFiles = testee.filter(MetaFileMap.of(
      "foo.bar",
      "foo.bar.xmp",
      "foo_01.bar.xmp",
      "foo_01.bar"
    ))

    assertThat(metaFiles.asMap())
      .containsOnlyKeys("foo.bar")
      .contains(entry("foo.bar", setOf("foo_01.bar.xmp","foo.bar.xmp","foo_01.bar")))
  }
}